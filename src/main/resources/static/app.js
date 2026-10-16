'use strict';

/* =========================================================================
 *  Motion Studio frontend (vanilla JS, talks to the Spring Boot REST API)
 * ========================================================================= */

const state = {
    meta: null,
    presets: [],
    scenes: [],
    session: null,
    editingId: null,
    view: 'studio',
};

const $ = (sel, root = document) => root.querySelector(sel);
const field = (form, name) => form.elements.namedItem(name);

/** Tiny DOM builder: h('div', {class: 'x', onclick: fn}, 'text', childNode) */
function h(tag, props = {}, ...children) {
    const el = document.createElement(tag);
    for (const [key, value] of Object.entries(props)) {
        if (value == null || value === false) continue;
        if (key === 'class') el.className = value;
        else if (key === 'style') Object.assign(el.style, value);
        else if (key.startsWith('on')) el.addEventListener(key.slice(2), value);
        else el.setAttribute(key, value);
    }
    for (const child of children.flat()) {
        if (child != null && child !== false) el.append(child instanceof Node ? child : String(child));
    }
    return el;
}

function debounce(fn, ms) {
    let t;
    return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), ms); };
}

/* ------------------------------------------------------------------ API */

async function api(path, { method = 'GET', body } = {}) {
    const res = await fetch('/api' + path, {
        method,
        headers: body ? { 'Content-Type': 'application/json' } : {},
        body: body ? JSON.stringify(body) : undefined,
    });
    if (res.status === 204) return null;
    const data = await res.json().catch(() => null);
    if (!res.ok) {
        const error = new Error(data?.message || res.statusText);
        error.fields = data?.fieldErrors;
        throw error;
    }
    return data;
}

function describeError(e) {
    if (!e.fields) return e.message;
    return e.message + ': ' + Object.entries(e.fields).map(([k, v]) => `${k} ${v}`).join(', ');
}

function toast(message, type = 'ok') {
    const el = h('div', { class: 'toast ' + type }, message);
    $('#toasts').append(el);
    setTimeout(() => { el.classList.add('out'); setTimeout(() => el.remove(), 300); }, 3200);
}

/* ------------------------------------------------------ animation engine */

function hexToRgb(hex) {
    const n = parseInt(hex.slice(1), 16);
    return [(n >> 16) & 255, (n >> 8) & 255, n & 255];
}

function mixColor(from, to, t) {
    const a = hexToRgb(from), b = hexToRgb(to);
    return `rgb(${a.map((v, i) => Math.round(v + (b[i] - v) * t)).join(',')})`;
}

function countUnits(text, preset) {
    return preset.split === 'WORD'
        ? text.split(/\s+/).filter(Boolean).length
        : [...text.replace(/\s/g, '')].length;
}

/** How long (ms) a single play of the animation takes; infinite loops count as two cycles. */
function animationLength(text, preset) {
    const cycles = preset.iterations === 0 ? 2 : preset.iterations;
    return preset.delayMs + Math.max(0, countUnits(text, preset) - 1) * preset.staggerMs + preset.durationMs * cycles;
}

/**
 * Renders text into target, splitting it into letters or words and giving each one
 * the preset's CSS animation with an increasing delay (stagger). Re-rendering restarts it.
 * look = {text, fontCss, fontSizePx, colorFrom, colorTo}
 */
function renderAnimated(target, look, preset, { fontScale = 1, maxFontPx = Infinity } = {}) {
    const size = Math.min(look.fontSizePx * fontScale, maxFontPx);
    const wrap = h('div', { class: 'ms-text', style: { fontFamily: look.fontCss, fontSize: size + 'px' } });
    const byWord = preset.split === 'WORD';
    const typewriter = preset.effect === 'TYPEWRITER';
    const easing = typewriter ? 'steps(1, end)' : preset.easingCss;
    const iterations = preset.iterations === 0 ? 'infinite' : preset.iterations;
    const total = Math.max(1, countUnits(look.text, preset));
    let index = 0;

    const unit = (content, isWord) => {
        const t = total === 1 ? 0 : index / (total - 1);
        const delay = preset.delayMs + index * preset.staggerMs;
        index++;
        return h('span', {
            class: 'ms-unit' + (isWord ? ' word' : ''),
            style: {
                color: mixColor(look.colorFrom, look.colorTo, t),
                animation: `${preset.cssName} ${preset.durationMs}ms ${easing} ${delay}ms ${iterations} both`,
            },
        }, content);
    };

    look.text.split('\n').forEach((line, lineNo) => {
        if (lineNo > 0) wrap.append(h('br'));
        for (const part of line.split(/(\s+)/)) {
            if (!part) continue;
            if (/^\s+$/.test(part)) wrap.append(' ');
            else if (byWord) wrap.append(unit(part, true));
            else wrap.append(h('span', { class: 'ms-word' }, [...part].map(ch => unit(ch, false))));
        }
    });
    if (typewriter) wrap.append(h('span', { class: 'ms-caret', style: { color: look.colorTo } }));

    target.replaceChildren(wrap);
}

const fontCss = code => state.meta.fonts.find(f => f.code === code)?.css ?? 'system-ui';
const sceneLook = scene => ({ ...scene, fontCss: scene.fontCss ?? fontCss(scene.font) });
const presetById = id => state.presets.find(p => p.id === Number(id));
const iterLabel = p => (p.iterations === 0 ? '∞' : '×' + p.iterations);

/* -------------------------------------------------------------- session */

async function refreshSession() {
    try {
        const s = await api('/session');
        state.session = s;
        $('#sessionChip').textContent =
            `sid ${s.id} · ${s.requestCount} req · ♥ ${s.likedSceneIds.length} · ${s.activeSessions} online`;
        renderRecent();
    } catch { /* not critical */ }
}

/* --------------------------------------------------------------- views */

const VIEWS = ['studio', 'presets', 'gallery'];

function switchView(name) {
    if (name === state.view || !VIEWS.includes(name)) return;
    history.replaceState(null, '', '#' + name);
    const apply = () => {
        state.view = name;
        document.querySelectorAll('.tab').forEach(t => t.classList.toggle('active', t.dataset.view === name));
        document.querySelectorAll('.view').forEach(v => v.classList.toggle('active', v.id === 'view-' + name));
        if (name === 'presets') renderPresets();
        if (name === 'gallery') loadScenes();
        if (name === 'studio') playStage();
    };
    if (document.startViewTransition) {
        document.startViewTransition(apply);
    } else {
        apply();
        const view = $('#view-' + name);
        view.classList.remove('fallback-in');
        void view.offsetWidth;
        view.classList.add('fallback-in');
    }
}

/* -------------------------------------------------------------- studio */

const sceneForm = $('#sceneForm');

function readSceneForm() {
    return {
        title: field(sceneForm, 'title').value.trim(),
        text: field(sceneForm, 'text').value,
        font: field(sceneForm, 'font').value,
        fontSizePx: Number(field(sceneForm, 'fontSizePx').value),
        colorFrom: field(sceneForm, 'colorFrom').value,
        colorTo: field(sceneForm, 'colorTo').value,
        presetId: Number(field(sceneForm, 'presetId').value),
    };
}

function playStage() {
    const data = readSceneForm();
    const preset = presetById(data.presetId);
    if (!preset) return;
    $('#sizeOut').textContent = data.fontSizePx;
    $('#presetHint').textContent =
        `${preset.effectLabel} · ${preset.durationMs}ms · stagger ${preset.staggerMs}ms · ${preset.easing.toLowerCase()} · ${iterLabel(preset)}`;
    document.querySelectorAll('#presetChips .chip')
        .forEach(c => c.classList.toggle('active', Number(c.dataset.id) === preset.id));
    const look = { ...data, text: data.text.trim() ? data.text : 'Type something…', fontCss: fontCss(data.font) };
    renderAnimated($('#stage'), look, preset);
}

function renderPresetOptions() {
    const select = $('#presetSelect');
    const current = select.value;
    select.replaceChildren(...state.presets.map(p => h('option', { value: p.id }, `${p.name} — ${p.effectLabel}`)));
    const fallback = state.presets.find(p => p.name === 'Spring Rise') ?? state.presets[0];
    select.value = presetById(current) ? current : fallback?.id ?? '';

    $('#presetChips').replaceChildren(...state.presets.map(p =>
        h('button', {
            class: 'chip', type: 'button', 'data-id': p.id, title: p.effectLabel,
            onclick: () => { select.value = p.id; playStage(); },
        }, p.name)));
}

function startEditing(scene) {
    state.editingId = scene.id;
    field(sceneForm, 'title').value = scene.title;
    field(sceneForm, 'text').value = scene.text;
    field(sceneForm, 'font').value = scene.font;
    field(sceneForm, 'fontSizePx').value = scene.fontSizePx;
    field(sceneForm, 'colorFrom').value = scene.colorFrom;
    field(sceneForm, 'colorTo').value = scene.colorTo;
    field(sceneForm, 'presetId').value = scene.preset.id;
    $('#editorTitle').textContent = 'Edit scene';
    $('#editingBadge').textContent = '#' + scene.id;
    $('#editingBadge').classList.remove('hidden');
    $('#updateBtn').classList.remove('hidden');
    if (state.view === 'studio') playStage();
    else switchView('studio');
}

function stopEditing() {
    state.editingId = null;
    $('#editorTitle').textContent = 'New scene';
    $('#editingBadge').classList.add('hidden');
    $('#updateBtn').classList.add('hidden');
}

async function saveScene(update) {
    if (!sceneForm.reportValidity()) return;
    const body = readSceneForm();
    try {
        const scene = update
            ? await api('/scenes/' + state.editingId, { method: 'PUT', body })
            : await api('/scenes', { method: 'POST', body });
        toast(update ? `Scene "${scene.title}" updated` : `Scene "${scene.title}" saved to the gallery`);
        startEditing(scene);
        refreshSession();
    } catch (e) {
        toast(describeError(e), 'error');
    }
}

sceneForm.addEventListener('submit', e => { e.preventDefault(); saveScene(false); });
$('#updateBtn').addEventListener('click', () => saveScene(true));
$('#playBtn').addEventListener('click', playStage);
$('#stage').addEventListener('click', playStage);
$('#resetBtn').addEventListener('click', () => {
    sceneForm.reset();
    stopEditing();
    renderPresetOptions();
    playStage();
});
const replaySoon = debounce(playStage, 350);
sceneForm.addEventListener('input', e => {
    if (e.target.name === 'fontSizePx') $('#sizeOut').textContent = e.target.value;
    if (e.target.name === 'presetId' || e.target.name === 'font') playStage();
    else replaySoon();
});

/* ------------------------------------------------------------- presets */

const PREVIEW_LOOK = { fontCss: "'Space Grotesk', sans-serif", fontSizePx: 34, colorFrom: '#f9a8d4', colorTo: '#a78bfa' };

async function loadPresets() {
    state.presets = await api('/presets');
    renderPresetOptions();
    if (state.view === 'presets') renderPresets();
}

function renderPresets() {
    const grid = $('#presetGrid');
    if (!state.presets.length) {
        grid.replaceChildren(h('p', { class: 'empty' }, 'No presets yet — create the first one!'));
        return;
    }
    grid.replaceChildren(...state.presets.map((p, i) => {
        const stage = h('div', { class: 'mini-stage', title: 'Hover or click to replay' });
        const play = () => renderAnimated(stage, { ...PREVIEW_LOOK, text: p.name }, p);
        stage.addEventListener('mouseenter', play);
        stage.addEventListener('click', play);
        play();
        return h('article', { class: 'card', style: { animationDelay: i * 35 + 'ms' } },
            stage,
            h('div', { class: 'card-body' },
                h('div', { class: 'card-title' }, p.name),
                h('div', { class: 'card-meta' },
                    `${p.effectLabel} · ${p.durationMs}ms · +${p.staggerMs}ms · ${p.easing.toLowerCase()} · ${iterLabel(p)}`),
                h('div', { class: 'card-actions' },
                    h('button', {
                        class: 'btn btn-sm', onclick: () => {
                            field(sceneForm, 'presetId').value = p.id;
                            switchView('studio');
                        },
                    }, 'Use in studio'),
                    h('button', { class: 'btn btn-sm', onclick: () => openPresetDialog(p) }, 'Edit'),
                    h('button', { class: 'btn btn-sm btn-danger', onclick: () => deletePreset(p) }, 'Delete'))));
    }));
}

async function deletePreset(p) {
    if (!confirm(`Delete preset "${p.name}"?`)) return;
    try {
        await api('/presets/' + p.id, { method: 'DELETE' });
        toast(`Preset "${p.name}" deleted`);
        await loadPresets();
    } catch (e) {
        toast(describeError(e), 'error');
    }
}

const presetDialog = $('#presetDialog');
const presetForm = $('#presetForm');
let editingPresetId = null;

function readPresetForm() {
    const num = name => Number(field(presetForm, name).value);
    return {
        name: field(presetForm, 'name').value.trim(),
        effect: field(presetForm, 'effect').value,
        easing: field(presetForm, 'easing').value,
        durationMs: num('durationMs'),
        delayMs: num('delayMs'),
        staggerMs: num('staggerMs'),
        iterations: num('iterations'),
    };
}

/** Builds a preset-like object from the dialog so it can be previewed before saving. */
function previewPresetFromForm() {
    const data = readPresetForm();
    const effect = state.meta.effects.find(e => e.code === data.effect);
    const easing = state.meta.easings.find(e => e.code === data.easing);
    $('#effectHint').textContent = effect.description;
    return { ...data, cssName: effect.cssName, split: effect.split, easingCss: easing.css };
}

function playPresetPreview() {
    const preset = previewPresetFromForm();
    renderAnimated($('#presetPreview'), { ...PREVIEW_LOOK, text: preset.name || 'Preview' }, preset);
}

function openPresetDialog(preset) {
    editingPresetId = preset?.id ?? null;
    presetForm.reset();
    $('#presetError').textContent = '';
    $('#presetDialogTitle').textContent = preset ? `Edit "${preset.name}"` : 'New preset';
    if (preset) {
        for (const key of ['name', 'effect', 'easing', 'durationMs', 'delayMs', 'staggerMs', 'iterations']) {
            field(presetForm, key).value = preset[key];
        }
    }
    presetDialog.showModal();
    playPresetPreview();
}

presetForm.addEventListener('input', debounce(playPresetPreview, 250));
$('#presetPreview').addEventListener('click', playPresetPreview);
$('#presetCancel').addEventListener('click', () => presetDialog.close());
$('#newPresetBtn').addEventListener('click', () => openPresetDialog(null));

presetForm.addEventListener('submit', async e => {
    e.preventDefault();
    const body = readPresetForm();
    try {
        const saved = editingPresetId
            ? await api('/presets/' + editingPresetId, { method: 'PUT', body })
            : await api('/presets', { method: 'POST', body });
        presetDialog.close();
        toast(`Preset "${saved.name}" saved`);
        await loadPresets();
    } catch (err) {
        $('#presetError').textContent = describeError(err);
    }
});

/* ------------------------------------------------------------- gallery */

async function loadScenes() {
    const q = $('#searchInput').value.trim();
    try {
        state.scenes = await api('/scenes' + (q ? '?q=' + encodeURIComponent(q) : ''));
        renderScenes();
        refreshSession();
    } catch (e) {
        toast(describeError(e), 'error');
    }
}

function renderScenes() {
    const grid = $('#sceneGrid');
    if (!state.scenes.length) {
        grid.replaceChildren(h('p', { class: 'empty' }, 'No scenes found. Create one in the Studio!'));
        return;
    }
    grid.replaceChildren(...state.scenes.map((scene, i) => sceneCard(scene, i)));
}

function sceneCard(scene, i) {
    const stage = h('div', { class: 'mini-stage', title: 'Click to play' });
    const meta = h('div', { class: 'card-meta' });
    const like = h('button', { class: 'btn btn-sm like' });
    const show = s => {
        meta.textContent = `${s.preset.name} · ▶ ${s.plays}`;
        like.textContent = '♥ ' + s.likes;
        like.classList.toggle('on', s.likedByMe);
    };
    const render = s => renderAnimated(stage, sceneLook(s), s.preset, { fontScale: 0.45, maxFontPx: 44 });
    const play = async () => {
        try {
            const updated = await api(`/scenes/${scene.id}/play`, { method: 'POST' });
            render(updated);
            show(updated);
            refreshSession();
        } catch (e) {
            toast(describeError(e), 'error');
        }
    };
    like.addEventListener('click', async () => {
        try {
            show(await api(`/scenes/${scene.id}/like`, { method: 'POST' }));
            refreshSession();
        } catch (e) {
            toast(describeError(e), 'error');
        }
    });
    stage.addEventListener('click', play);
    render(scene);
    show(scene);

    return h('article', { class: 'card', style: { animationDelay: i * 40 + 'ms' } },
        stage,
        h('div', { class: 'card-body' },
            h('div', { class: 'card-title' }, scene.title),
            meta,
            h('div', { class: 'card-actions' },
                h('button', { class: 'btn btn-sm', onclick: play }, '▶ Play'),
                h('button', { class: 'btn btn-sm', onclick: () => startEditing(scene) }, 'Edit'),
                h('button', { class: 'btn btn-sm btn-danger', onclick: () => deleteScene(scene) }, 'Delete'),
                like)));
}

async function deleteScene(scene) {
    if (!confirm(`Delete scene "${scene.title}"?`)) return;
    try {
        await api('/scenes/' + scene.id, { method: 'DELETE' });
        toast(`Scene "${scene.title}" deleted`);
        if (state.editingId === scene.id) stopEditing();
        loadScenes();
    } catch (e) {
        toast(describeError(e), 'error');
    }
}

function renderRecent() {
    const row = $('#recentRow');
    const recent = (state.session?.recentlyPlayed ?? [])
        .map(id => state.scenes.find(s => s.id === id))
        .filter(Boolean);
    row.classList.toggle('hidden', recent.length === 0);
    row.replaceChildren('Recently played in this session:',
        ...recent.map(s => h('button', { class: 'chip', onclick: () => startEditing(s) }, s.title)));
}

$('#searchInput').addEventListener('input', debounce(loadScenes, 250));

/* ------------------------------------------------------------ showcase */

const showcase = { list: [], index: 0, timer: null };

async function openShowcase() {
    await loadScenes();
    if (!state.scenes.length) {
        toast('Nothing to show yet — save a scene first', 'error');
        return;
    }
    showcase.list = [...state.scenes];
    showcase.index = 0;
    $('#showcase').classList.remove('hidden');
    $('#showcaseDots').replaceChildren(...showcase.list.map(s => h('span', { title: s.title })));
    showSlide();
}

function showSlide() {
    clearTimeout(showcase.timer);
    const scene = showcase.list[showcase.index];
    const stage = $('#showcaseStage');
    stage.classList.remove('leaving');
    renderAnimated(stage, sceneLook(scene), scene.preset, { fontScale: 1.5, maxFontPx: Math.max(48, innerWidth / 9) });
    [...$('#showcaseDots').children].forEach((d, i) => d.classList.toggle('on', i === showcase.index));
    api(`/scenes/${scene.id}/play`, { method: 'POST' }).catch(() => {});
    showcase.timer = setTimeout(() => nextSlide(1), animationLength(scene.text, scene.preset) + 2200);
}

function nextSlide(step) {
    clearTimeout(showcase.timer);
    $('#showcaseStage').classList.add('leaving');
    showcase.timer = setTimeout(() => {
        showcase.index = (showcase.index + step + showcase.list.length) % showcase.list.length;
        showSlide();
    }, 500);
}

function closeShowcase() {
    clearTimeout(showcase.timer);
    $('#showcase').classList.add('hidden');
    $('#showcaseStage').replaceChildren();
    if (state.view === 'gallery') loadScenes();
    else refreshSession();
}

$('#showcaseBtn').addEventListener('click', openShowcase);
$('#scPrev').addEventListener('click', () => nextSlide(-1));
$('#scNext').addEventListener('click', () => nextSlide(1));
$('#scClose').addEventListener('click', closeShowcase);
document.addEventListener('keydown', e => {
    if ($('#showcase').classList.contains('hidden')) return;
    if (e.key === 'Escape') closeShowcase();
    if (e.key === 'ArrowRight') nextSlide(1);
    if (e.key === 'ArrowLeft') nextSlide(-1);
});

/* ---------------------------------------------------------------- init */

function animateBrand() {
    renderAnimated($('#brand'),
        { text: 'Motion Studio', fontCss: "'Playfair Display', serif", fontSizePx: 24, colorFrom: '#f472b6', colorTo: '#a78bfa' },
        { split: 'CHAR', cssName: 'ms-rise', easingCss: 'cubic-bezier(0.34, 1.56, 0.64, 1)', durationMs: 700, delayMs: 100, staggerMs: 40, iterations: 1 });
}

async function init() {
    animateBrand();
    $('#brand').addEventListener('mouseenter', animateBrand);
    document.querySelectorAll('.tab').forEach(t => t.addEventListener('click', () => switchView(t.dataset.view)));

    try {
        state.meta = await api('/meta');
        $('#fontSelect').replaceChildren(...state.meta.fonts.map(f => h('option', { value: f.code }, f.label)));
        $('#effectSelect').replaceChildren(...state.meta.effects.map(e => h('option', { value: e.code }, e.label)));
        $('#easingSelect').replaceChildren(...state.meta.easings.map(e => h('option', { value: e.code }, e.label)));
        await loadPresets();
        await document.fonts.ready;
        playStage();
        await loadScenes();
        switchView(location.hash.slice(1));
    } catch (e) {
        toast('Cannot reach the API: ' + describeError(e), 'error');
    }
}

init();
