// =======================================================================
// CLASH OF BYTES: COMPUTER GRAPHICS (CG) ACADEMIC PRACTICALS MODULE
// Course Code: 2310218L | Semester III | Academic Year 2026-2027
// Tasks 1 to 4 Complete Interactive Suite & C++ OpenGL Algorithmic Demos
// =======================================================================

// State for interactive canvases
const cgState = {
  // Prac 05: Cohen-Sutherland Clipping
  clipping: {
    xmin: 200, ymin: 100, xmax: 660, ymax: 340, // Viewport scaled for canvas
    p0: { x: 80, y: 390 },
    p1: { x: 780, y: 60 },
    draggingPoint: null,
    inGameLaserAngle: 0,
    autoLaserActive: false
  },
  // Prac 02A: Bresenham Line
  bresenhamLine: {
    x1: 4, y1: 3, x2: 24, y2: 12,
    gridSize: 20,
    dragging: null
  },
  // Prac 02B: Bresenham Circle
  bresenhamCircle: {
    xc: 20, yc: 14, radius: 10,
    gridSize: 15,
    multiColorOctants: true
  },
  // Prac 03: Scanline Polygon Fill
  scanline: {
    shape: 'healthbar',
    hpPercent: 85,
    sweepY: null,
    autoSweep: true,
    sweepDir: 1
  },
  // Prac 04: 3D Transformations
  transform3D: {
    rotX: 25, rotY: 35, rotZ: 15,
    scale: 1.0,
    transX: 0, transY: 0,
    autoSpin: true,
    objectType: 'cube' // 'cube' or 'shuriken'
  },
  // Prac 06: Textures & Lighting
  lighting: {
    lightX: 300, lightY: 150,
    ambient: 0.25, diffuse: 0.85, specular: 0.95,
    shininess: 64,
    mode: 'dynamic', // 'dynamic', 'key', 'unlit'
    textureType: 'floor', // 'floor', 'pillar', 'shield', 'circuit'
    materialType: 'cyber'
  },
  // Prac 01: Gouraud Shading
  gouraud: {
    c1: '#00e5ff', c2: '#ff2a6d', c3: '#ffcc00', c4: '#00ff88',
    primitive: 'quad'
  },
  // Task 4: AABB Collision
  collision: {
    box1: { x: 140, y: 160, w: 90, h: 140, label: 'P1 Hurtbox' },
    box2: { x: 260, y: 180, w: 90, h: 100, label: 'P2 Attack Hitbox' },
    draggingBox: null,
    dragOffset: { x: 0, y: 0 }
  }
};

// -----------------------------------------------------------------------
// 1. PRACTICAL 05: COHEN-SUTHERLAND 2D LINE CLIPPING ALGORITHM (CO5 / TASK 2)
// -----------------------------------------------------------------------
const OUT_INSIDE = 0; // 0000
const OUT_LEFT   = 1; // 0001
const OUT_RIGHT  = 2; // 0010
const OUT_BOTTOM = 4; // 0100
const OUT_TOP    = 8; // 1000

function computeOutCode(x, y, xmin, ymin, xmax, ymax) {
  let code = OUT_INSIDE;
  if (x < xmin) code |= OUT_LEFT;
  else if (x > xmax) code |= OUT_RIGHT;
  if (y < ymin) code |= OUT_TOP; // Canvas Y goes down
  else if (y > ymax) code |= OUT_BOTTOM;
  return code;
}

function outcodeToBinaryString(code) {
  let s = '';
  s += (code & OUT_TOP) ? '1' : '0';
  s += (code & OUT_BOTTOM) ? '1' : '0';
  s += (code & OUT_RIGHT) ? '1' : '0';
  s += (code & OUT_LEFT) ? '1' : '0';
  return s;
}

export function clipLineCohenSutherland(x0, y0, x1, y1, xmin, ymin, xmax, ymax) {
  let code0 = computeOutCode(x0, y0, xmin, ymin, xmax, ymax);
  let code1 = computeOutCode(x1, y1, xmin, ymin, xmax, ymax);
  let accept = false;
  const steps = [];

  steps.push({
    step: 'Initial Outcodes',
    p0Out: outcodeToBinaryString(code0),
    p1Out: outcodeToBinaryString(code1),
    p0: { x: Math.round(x0), y: Math.round(y0) },
    p1: { x: Math.round(x1), y: Math.round(y1) }
  });

  let iter = 0;
  while (iter < 10) {
    iter++;
    if ((code0 | code1) === 0) {
      // Trivial accept
      accept = true;
      steps.push({ step: 'Trivial Accept', detail: 'Both endpoints inside viewport: (code0 | code1) === 0000' });
      break;
    } else if ((code0 & code1) !== 0) {
      // Trivial reject
      steps.push({ step: 'Trivial Reject', detail: 'Both endpoints share outside zone: (code0 & code1) !== 0000' });
      break;
    } else {
      // Line crosses clipping boundary; calculate intersection
      let x = 0, y = 0;
      const codeOut = code0 !== 0 ? code0 : code1;
      let boundaryName = '';

      if (codeOut & OUT_TOP) {
        x = x0 + (x1 - x0) * (ymin - y0) / (y1 - y0);
        y = ymin;
        boundaryName = 'TOP (Y=' + Math.round(ymin) + ')';
      } else if (codeOut & OUT_BOTTOM) {
        x = x0 + (x1 - x0) * (ymax - y0) / (y1 - y0);
        y = ymax;
        boundaryName = 'BOTTOM (Y=' + Math.round(ymax) + ')';
      } else if (codeOut & OUT_RIGHT) {
        y = y0 + (y1 - y0) * (xmax - x0) / (x1 - x0);
        x = xmax;
        boundaryName = 'RIGHT (X=' + Math.round(xmax) + ')';
      } else if (codeOut & OUT_LEFT) {
        y = y0 + (y1 - y0) * (xmin - x0) / (x1 - x0);
        x = xmin;
        boundaryName = 'LEFT (X=' + Math.round(xmin) + ')';
      }

      steps.push({
        step: `Clip against ${boundaryName}`,
        clippedPoint: (codeOut === code0) ? 'P0' : 'P1',
        newIntersection: { x: Math.round(x), y: Math.round(y) }
      });

      if (codeOut === code0) {
        x0 = x; y0 = y;
        code0 = computeOutCode(x0, y0, xmin, ymin, xmax, ymax);
      } else {
        x1 = x; y1 = y;
        code1 = computeOutCode(x1, y1, xmin, ymin, xmax, ymax);
      }
    }
  }

  return {
    visible: accept,
    clippedP0: { x: x0, y: y0 },
    clippedP1: { x: x1, y: y1 },
    steps: steps
  };
}

export function drawClippingCanvas() {
  const canvas = document.getElementById('cgCanvasClipping');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  const w = canvas.width;
  const h = canvas.height;

  // Clear background
  ctx.fillStyle = '#060913';
  ctx.fillRect(0, 0, w, h);

  const { xmin, ymin, xmax, ymax, p0, p1 } = cgState.clipping;

  // Draw 9-Region Grid Lines
  ctx.strokeStyle = '#1e293b';
  ctx.lineWidth = 1;
  ctx.setLineDash([4, 4]);

  // Vertical region boundaries
  ctx.beginPath();
  ctx.moveTo(xmin, 0); ctx.lineTo(xmin, h);
  ctx.moveTo(xmax, 0); ctx.lineTo(xmax, h);
  // Horizontal region boundaries
  ctx.moveTo(0, ymin); ctx.lineTo(w, ymin);
  ctx.moveTo(0, ymax); ctx.lineTo(w, ymax);
  ctx.stroke();
  ctx.setLineDash([]);

  // Region Labels (4-bit binary outcodes)
  ctx.font = '11px "Orbitron", monospace';
  ctx.fillStyle = '#475569';
  ctx.textAlign = 'center';
  ctx.fillText('TOP-LEFT: 1001', xmin / 2, ymin / 2);
  ctx.fillText('TOP: 1000', (xmin + xmax) / 2, ymin / 2);
  ctx.fillText('TOP-RIGHT: 1010', (xmax + w) / 2, ymin / 2);

  ctx.fillText('LEFT: 0001', xmin / 2, (ymin + ymax) / 2);
  ctx.fillText('RIGHT: 0010', (xmax + w) / 2, (ymin + ymax) / 2);

  ctx.fillText('BOTTOM-LEFT: 0101', xmin / 2, (ymax + h) / 2);
  ctx.fillText('BOTTOM: 0100', (xmin + xmax) / 2, (ymax + h) / 2);
  ctx.fillText('BOTTOM-RIGHT: 0110', (xmax + w) / 2, (ymax + h) / 2);

  // Draw Viewport Window (INSIDE Region: 0000)
  ctx.fillStyle = 'rgba(0, 229, 255, 0.05)';
  ctx.fillRect(xmin, ymin, xmax - xmin, ymax - ymin);
  ctx.strokeStyle = '#00e5ff';
  ctx.lineWidth = 2;
  ctx.strokeRect(xmin, ymin, xmax - xmin, ymax - ymin);

  // Viewport Header Label
  ctx.fillStyle = '#00e5ff';
  ctx.font = 'bold 12px "Orbitron", sans-serif';
  ctx.fillText('CLIPPING VIEWPORT (INSIDE: 0000)', (xmin + xmax) / 2, ymin + 20);
  ctx.font = '10px monospace';
  ctx.fillStyle = '#94a3b8';
  ctx.fillText(`Bounds: [${Math.round(xmin)}, ${Math.round(ymin)}] to [${Math.round(xmax)}, ${Math.round(ymax)}]`, (xmin + xmax) / 2, ymin + 36);

  // Calculate Cohen-Sutherland Clipping
  const clipResult = clipLineCohenSutherland(p0.x, p0.y, p1.x, p1.y, xmin, ymin, xmax, ymax);

  // Draw Full Unclipped Original Line (Faint Red / Dashed to show what was cut)
  ctx.beginPath();
  ctx.strokeStyle = 'rgba(255, 42, 109, 0.45)';
  ctx.lineWidth = 2;
  ctx.setLineDash([5, 5]);
  ctx.moveTo(p0.x, p0.y);
  ctx.lineTo(p1.x, p1.y);
  ctx.stroke();
  ctx.setLineDash([]);

  // Draw Clipped Line (Solid Bright Neon Green) if accepted
  if (clipResult.visible) {
    ctx.beginPath();
    ctx.strokeStyle = '#00ff88';
    ctx.lineWidth = 4;
    ctx.shadowColor = '#00ff88';
    ctx.shadowBlur = 12;
    ctx.moveTo(clipResult.clippedP0.x, clipResult.clippedP0.y);
    ctx.lineTo(clipResult.clippedP1.x, clipResult.clippedP1.y);
    ctx.stroke();
    ctx.shadowBlur = 0; // reset

    // Draw clipped boundary intersection points
    ctx.fillStyle = '#ffcc00';
    ctx.beginPath();
    ctx.arc(clipResult.clippedP0.x, clipResult.clippedP0.y, 5, 0, Math.PI * 2);
    ctx.arc(clipResult.clippedP1.x, clipResult.clippedP1.y, 5, 0, Math.PI * 2);
    ctx.fill();
  }

  // Draw Interactive Draggable Endpoint P0
  drawPointHandle(ctx, p0.x, p0.y, 'P0', '#00e5ff', computeOutCode(p0.x, p0.y, xmin, ymin, xmax, ymax));
  // Draw Interactive Draggable Endpoint P1
  drawPointHandle(ctx, p1.x, p1.y, 'P1', '#ff2a6d', computeOutCode(p1.x, p1.y, xmin, ymin, xmax, ymax));

  // Update Telemetry & Step Log Panel
  updateClippingTelemetry(clipResult);
}

function drawPointHandle(ctx, x, y, label, color, outcode) {
  ctx.save();
  ctx.fillStyle = color;
  ctx.shadowColor = color;
  ctx.shadowBlur = 10;
  ctx.beginPath();
  ctx.arc(x, y, 8, 0, Math.PI * 2);
  ctx.fill();
  ctx.shadowBlur = 0;

  ctx.strokeStyle = '#fff';
  ctx.lineWidth = 2;
  ctx.beginPath();
  ctx.arc(x, y, 8, 0, Math.PI * 2);
  ctx.stroke();

  // Label
  ctx.fillStyle = '#fff';
  ctx.font = 'bold 12px "Rajdhani", sans-serif';
  ctx.textAlign = 'left';
  const outStr = outcodeToBinaryString(outcode);
  ctx.fillText(`${label} (${Math.round(x)}, ${Math.round(y)}) [${outStr}]`, x + 12, y - 6);
  ctx.restore();
}

function updateClippingTelemetry(result) {
  const statusEl = document.getElementById('cgClipStatus');
  const detailsEl = document.getElementById('cgClipDetails');
  if (!statusEl || !detailsEl) return;

  if (result.visible) {
    statusEl.innerHTML = '<span class="badge badge-win" style="font-size:0.9rem;">ACCEPTED (VISIBLE IN VIEWPORT)</span>';
  } else {
    statusEl.innerHTML = '<span class="badge badge-loss" style="font-size:0.9rem;">TRIVIAL REJECT (COMPLETELY OUTSIDE)</span>';
  }

  let html = '<div style="font-family:monospace; font-size:0.85rem; line-height:1.6; color:#cbd5e1;">';
  result.steps.forEach((st, idx) => {
    html += `<div><strong style="color:var(--neon-cyan);">[Step ${idx + 1}] ${st.step}:</strong> `;
    if (st.p0Out) {
      html += `P0 Outcode: <span style="color:#00e5ff;">${st.p0Out}</span> | P1 Outcode: <span style="color:#ff2a6d;">${st.p1Out}</span>`;
    } else if (st.detail) {
      html += `<span style="color:${st.step.includes('Accept') ? '#00ff88' : '#ff2a6d'};">${st.detail}</span>`;
    } else if (st.newIntersection) {
      html += `Clipped ${st.clippedPoint} -> New Coord: (${st.newIntersection.x}, ${st.newIntersection.y})`;
    }
    html += '</div>';
  });

  if (result.visible) {
    html += `<div style="margin-top:6px; color:#00ff88; font-weight:bold;">Final Visible Span: (${Math.round(result.clippedP0.x)}, ${Math.round(result.clippedP0.y)}) &rarr; (${Math.round(result.clippedP1.x)}, ${Math.round(result.clippedP1.y)})</div>`;
  }
  html += '</div>';
  detailsEl.innerHTML = html;
}

// -----------------------------------------------------------------------
// 2. PRACTICAL 02A: BRESENHAM LINE DRAWING ALGORITHM (CO3 / TASK 1 & 4)
// -----------------------------------------------------------------------
export function drawBresenhamLineCanvas() {
  const canvas = document.getElementById('cgCanvasBresenhamLine');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  const w = canvas.width;
  const h = canvas.height;

  ctx.fillStyle = '#060913';
  ctx.fillRect(0, 0, w, h);

  const { x1, y1, x2, y2, gridSize } = cgState.bresenhamLine;
  const cols = Math.floor(w / gridSize);
  const rows = Math.floor(h / gridSize);

  // Draw Grid Lines
  ctx.strokeStyle = '#0f172a';
  ctx.lineWidth = 1;
  for (let c = 0; c <= cols; c++) {
    ctx.beginPath();
    ctx.moveTo(c * gridSize, 0);
    ctx.lineTo(c * gridSize, h);
    ctx.stroke();
  }
  for (let r = 0; r <= rows; r++) {
    ctx.beginPath();
    ctx.moveTo(0, r * gridSize);
    ctx.lineTo(w, r * gridSize);
    ctx.stroke();
  }

  // Draw Ideal Continuous Floating-Point Line (Faint Reference)
  ctx.beginPath();
  ctx.strokeStyle = 'rgba(255, 204, 0, 0.4)';
  ctx.lineWidth = 2;
  ctx.moveTo(x1 * gridSize + gridSize / 2, y1 * gridSize + gridSize / 2);
  ctx.lineTo(x2 * gridSize + gridSize / 2, y2 * gridSize + gridSize / 2);
  ctx.stroke();

  // Run Bresenham Algorithm Step-by-Step
  let curX = x1, curY = y1;
  const dx = Math.abs(x2 - x1);
  const dy = Math.abs(y2 - y1);
  const sx = (x1 < x2) ? 1 : -1;
  const sy = (y1 < y2) ? 1 : -1;
  let err = dx - dy;

  const points = [];
  while (true) {
    points.push({ x: curX, y: curY, err: err });

    // Fill pixel cell
    ctx.fillStyle = 'rgba(0, 229, 255, 0.85)';
    ctx.fillRect(curX * gridSize + 1, curY * gridSize + 1, gridSize - 2, gridSize - 2);

    if (curX === x2 && curY === y2) break;
    const e2 = 2 * err;
    if (e2 > -dy) {
      err -= dy;
      curX += sx;
    }
    if (e2 < dx) {
      err += dx;
      curY += sy;
    }
  }

  // Highlight Start & End Endpoints
  ctx.fillStyle = '#ff2a6d';
  ctx.fillRect(x1 * gridSize + 1, y1 * gridSize + 1, gridSize - 2, gridSize - 2);
  ctx.fillStyle = '#00ff88';
  ctx.fillRect(x2 * gridSize + 1, y2 * gridSize + 1, gridSize - 2, gridSize - 2);

  // Update Readout Table
  const logEl = document.getElementById('cgBresenhamLineLog');
  if (logEl) {
    let html = `<div style="font-family:monospace; font-size:0.8rem; color:#94a3b8; margin-bottom:4px;">
      Line from (${x1}, ${y1}) to (${x2}, ${y2}) &bull; &Delta;X = ${dx}, &Delta;Y = ${dy}, Initial Err = ${dx - dy}
    </div>`;
    html += '<div style="display:flex; flex-wrap:wrap; gap:6px; max-height:80px; overflow-y:auto;">';
    points.forEach((p, idx) => {
      html += `<span class="badge" style="background:#0f172a; color:#00e5ff; border:1px solid #1e293b;">#${idx}: (${p.x}, ${p.y})</span>`;
    });
    html += '</div>';
    logEl.innerHTML = html;
  }
}

// -----------------------------------------------------------------------
// 3. PRACTICAL 02B: BRESENHAM 8-WAY SYMMETRIC CIRCLE (CO3 / TASK 1 & 4)
// -----------------------------------------------------------------------
export function drawBresenhamCircleCanvas() {
  const canvas = document.getElementById('cgCanvasBresenhamCircle');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  const w = canvas.width;
  const h = canvas.height;

  ctx.fillStyle = '#060913';
  ctx.fillRect(0, 0, w, h);

  const { xc, yc, radius, gridSize, multiColorOctants } = cgState.bresenhamCircle;
  const cols = Math.floor(w / gridSize);
  const rows = Math.floor(h / gridSize);

  // Grid
  ctx.strokeStyle = '#0f172a';
  ctx.lineWidth = 1;
  for (let c = 0; c <= cols; c++) {
    ctx.beginPath(); ctx.moveTo(c * gridSize, 0); ctx.lineTo(c * gridSize, h); ctx.stroke();
  }
  for (let r = 0; r <= rows; r++) {
    ctx.beginPath(); ctx.moveTo(0, r * gridSize); ctx.lineTo(w, r * gridSize); ctx.stroke();
  }

  // Draw Ideal Circle Outline
  ctx.beginPath();
  ctx.strokeStyle = 'rgba(255, 204, 0, 0.35)';
  ctx.lineWidth = 1.5;
  ctx.arc(xc * gridSize + gridSize / 2, yc * gridSize + gridSize / 2, radius * gridSize, 0, Math.PI * 2);
  ctx.stroke();

  // 8 Octant Colors
  const octantColors = [
    '#00e5ff', '#ff2a6d', '#ffcc00', '#00ff88',
    '#38bdf8', '#c084fc', '#f43f5e', '#a3e635'
  ];

  let x = 0;
  let y = radius;
  let d = 3 - 2 * radius;
  const points = [];

  function plotPixel(px, py, octantIdx) {
    if (px >= 0 && px < cols && py >= 0 && py < rows) {
      ctx.fillStyle = multiColorOctants ? octantColors[octantIdx % 8] : '#00e5ff';
      ctx.fillRect(px * gridSize + 1, py * gridSize + 1, gridSize - 2, gridSize - 2);
    }
  }

  while (y >= x) {
    points.push({ x: x, y: y, d: d });

    // Plot 8 Octants
    plotPixel(xc + x, yc + y, 0);
    plotPixel(xc - x, yc + y, 1);
    plotPixel(xc + x, yc - y, 2);
    plotPixel(xc - x, yc - y, 3);
    plotPixel(xc + y, yc + x, 4);
    plotPixel(xc - y, yc + x, 5);
    plotPixel(xc + y, yc - x, 6);
    plotPixel(xc - y, yc - x, 7);

    if (d > 0) {
      y--;
      d = d + 4 * (x - y) + 10;
    } else {
      d = d + 4 * x + 6;
    }
    x++;
  }

  // Center Point
  ctx.fillStyle = '#fff';
  ctx.beginPath();
  ctx.arc(xc * gridSize + gridSize / 2, yc * gridSize + gridSize / 2, 4, 0, Math.PI * 2);
  ctx.fill();

  const logEl = document.getElementById('cgBresenhamCircleLog');
  if (logEl) {
    logEl.innerHTML = `<span style="color:#94a3b8;">Center:</span> (${xc}, ${yc}) &bull; <span style="color:#94a3b8;">Radius:</span> ${radius} &bull; <span style="color:#94a3b8;">Initial d0:</span> ${3 - 2 * radius} &bull; <span style="color:#00ff88;">Octant Points Generated:</span> ${points.length} (Total Circumference: ${points.length * 8})`;
  }
}

// -----------------------------------------------------------------------
// 4. PRACTICAL 03: SCAN-LINE POLYGON FILLING (CO3 / TASK 4)
// -----------------------------------------------------------------------
export function drawScanLineCanvas() {
  const canvas = document.getElementById('cgCanvasScanline');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  const w = canvas.width;
  const h = canvas.height;

  ctx.fillStyle = '#060913';
  ctx.fillRect(0, 0, w, h);

  let vertices = [];
  const { shape, hpPercent } = cgState.scanline;

  if (shape === 'healthbar') {
    // Dynamic In-Game Health Bar Trapezoid
    const barWidth = 600 * (hpPercent / 100);
    vertices = [
      { x: 80, y: 140 },
      { x: 80 + barWidth, y: 140 },
      { x: 60 + barWidth, y: 220 },
      { x: 60, y: 220 }
    ];
  } else if (shape === 'hexagon') {
    // Combat Barrier Shield Hexagon
    vertices = [
      { x: 380, y: 80 },
      { x: 500, y: 150 },
      { x: 500, y: 270 },
      { x: 380, y: 340 },
      { x: 260, y: 270 },
      { x: 260, y: 150 }
    ];
  } else {
    // Diamond Crest
    vertices = [
      { x: 380, y: 70 },
      { x: 540, y: 210 },
      { x: 380, y: 350 },
      { x: 220, y: 210 }
    ];
  }

  // Find minY and maxY
  let minY = vertices[0].y, maxY = vertices[0].y;
  vertices.forEach(v => {
    if (v.y < minY) minY = v.y;
    if (v.y > maxY) maxY = v.y;
  });

  // Handle animation / sweep line
  if (cgState.scanline.sweepY === null) cgState.scanline.sweepY = minY;
  if (cgState.scanline.autoSweep) {
    cgState.scanline.sweepY += 2 * cgState.scanline.sweepDir;
    if (cgState.scanline.sweepY > maxY) {
      cgState.scanline.sweepY = maxY;
      cgState.scanline.sweepDir = -1;
    } else if (cgState.scanline.sweepY < minY) {
      cgState.scanline.sweepY = minY;
      cgState.scanline.sweepDir = 1;
    }
  }

  const activeSweep = Math.round(cgState.scanline.sweepY);

  // Rasterize Scan-Line Spans from minY to activeSweep
  ctx.strokeStyle = (shape === 'healthbar') ? '#00e5ff' : '#00ff88';
  ctx.lineWidth = 1;

  for (let y = minY; y <= activeSweep; y++) {
    const intersections = [];
    const n = vertices.length;

    for (let i = 0; i < n; i++) {
      const j = (i + 1) % n;
      const y1 = vertices[i].y, y2 = vertices[j].y;
      const x1 = vertices[i].x, x2 = vertices[j].x;

      if ((y1 <= y && y2 > y) || (y2 <= y && y1 > y)) {
        const x = x1 + (y - y1) / (y2 - y1) * (x2 - x1);
        intersections.push(Math.round(x));
      }
    }

    intersections.sort((a, b) => a - b);

    // Fill spans between pairs (Parity Checking)
    for (let k = 0; k + 1 < intersections.length; k += 2) {
      ctx.beginPath();
      ctx.moveTo(intersections[k], y);
      ctx.lineTo(intersections[k + 1], y);
      ctx.stroke();
    }

    // Highlight the active sweep line in neon gold with intersection dots
    if (y === activeSweep) {
      ctx.save();
      ctx.strokeStyle = '#ffcc00';
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(w, y);
      ctx.stroke();

      intersections.forEach(ix => {
        ctx.fillStyle = '#ff2a6d';
        ctx.beginPath();
        ctx.arc(ix, y, 5, 0, Math.PI * 2);
        ctx.fill();
      });
      ctx.restore();
    }
  }

  // Draw Polygon Wireframe Perimeter
  ctx.strokeStyle = '#fff';
  ctx.lineWidth = 2;
  ctx.beginPath();
  ctx.moveTo(vertices[0].x, vertices[0].y);
  for (let i = 1; i < vertices.length; i++) {
    ctx.lineTo(vertices[i].x, vertices[i].y);
  }
  ctx.closePath();
  ctx.stroke();

  // Update Status Log
  const logEl = document.getElementById('cgScanlineLog');
  if (logEl) {
    logEl.innerHTML = `<span style="color:#94a3b8;">Active Scanline Y:</span> <strong style="color:#ffcc00;">${activeSweep}</strong> &bull; <span style="color:#94a3b8;">Range:</span> [${minY} .. ${maxY}] &bull; <span style="color:#00e5ff;">HP Level:</span> ${hpPercent}% &bull; <span style="color:#00ff88;">Parity Spans Active</span>`;
  }
}

// -----------------------------------------------------------------------
// 5. PRACTICAL 04: TRANSFORMATION MATRICES & 3D CUBE (CO2 / TASK 2)
// -----------------------------------------------------------------------
export function draw3DTransformCanvas() {
  const canvas = document.getElementById('cgCanvasTransform3D');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  const w = canvas.width;
  const h = canvas.height;

  ctx.fillStyle = '#060913';
  ctx.fillRect(0, 0, w, h);

  const { rotX, rotY, rotZ, scale, transX, transY, objectType } = cgState.transform3D;

  if (cgState.transform3D.autoSpin) {
    cgState.transform3D.rotX = (cgState.transform3D.rotX + 0.8) % 360;
    cgState.transform3D.rotY = (cgState.transform3D.rotY + 1.2) % 360;
    cgState.transform3D.rotZ = (cgState.transform3D.rotZ + 0.5) % 360;
  }

  const cx = w / 2 + transX;
  const cy = h / 2 + transY;

  const radX = (rotX * Math.PI) / 180;
  const radY = (rotY * Math.PI) / 180;
  const radZ = (rotZ * Math.PI) / 180;

  function rotate3D(x, y, z) {
    // Rotation around X
    let y1 = y * Math.cos(radX) - z * Math.sin(radX);
    let z1 = y * Math.sin(radX) + z * Math.cos(radX);
    let x1 = x;

    // Rotation around Y
    let x2 = x1 * Math.cos(radY) + z1 * Math.sin(radY);
    let z2 = -x1 * Math.sin(radY) + z1 * Math.cos(radY);
    let y2 = y1;

    // Rotation around Z
    let x3 = x2 * Math.cos(radZ) - y2 * Math.sin(radZ);
    let y3 = x2 * Math.sin(radZ) + y2 * Math.cos(radZ);
    let z3 = z2;

    return { x: x3, y: y3, z: z3 };
  }

  function project(p, size) {
    const fov = 350;
    const distance = 400;
    const factor = fov / (distance + p.z * size);
    return {
      x: cx + p.x * size * factor * scale,
      y: cy + p.y * size * factor * scale,
      z: p.z
    };
  }

  if (objectType === 'cube') {
    // 3D Cube Vertices (Matching TransformationMatrix3DCube.hpp)
    const rawVertices = [
      { x: -1, y: -1, z:  1 },
      { x:  1, y: -1, z:  1 },
      { x:  1, y:  1, z:  1 },
      { x: -1, y:  1, z:  1 },
      { x: -1, y: -1, z: -1 },
      { x: -1, y:  1, z: -1 },
      { x:  1, y:  1, z: -1 },
      { x:  1, y: -1, z: -1 }
    ];

    const rotVerts = rawVertices.map(v => rotate3D(v.x, v.y, v.z));
    const projVerts = rotVerts.map(v => project(v, 90));

    // Faces: Front (Red), Back (Green), Top (Blue), Bottom (Yellow), Right (Magenta), Left (Cyan)
    const faces = [
      { indices: [0, 1, 2, 3], color: 'rgba(230, 50, 50, 0.85)', label: 'Front (Red)' },
      { indices: [4, 5, 6, 7], color: 'rgba(50, 230, 80, 0.85)', label: 'Back (Green)' },
      { indices: [5, 3, 2, 6], color: 'rgba(50, 100, 240, 0.85)', label: 'Top (Blue)' },
      { indices: [4, 7, 1, 0], color: 'rgba(240, 230, 50, 0.85)', label: 'Bottom (Yellow)' },
      { indices: [1, 7, 6, 2], color: 'rgba(230, 50, 230, 0.85)', label: 'Right (Magenta)' },
      { indices: [4, 0, 3, 5], color: 'rgba(50, 230, 230, 0.85)', label: 'Left (Cyan)' }
    ];

    // Compute face depths for Painter's algorithm
    faces.forEach(face => {
      let avgZ = 0;
      face.indices.forEach(idx => { avgZ += rotVerts[idx].z; });
      face.depth = avgZ / 4;
    });

    faces.sort((a, b) => a.depth - b.depth);

    // Draw sorted faces
    faces.forEach(face => {
      ctx.beginPath();
      const p0 = projVerts[face.indices[0]];
      ctx.moveTo(p0.x, p0.y);
      for (let i = 1; i < 4; i++) {
        const p = projVerts[face.indices[i]];
        ctx.lineTo(p.x, p.y);
      }
      ctx.closePath();
      ctx.fillStyle = face.color;
      ctx.fill();
      ctx.strokeStyle = '#fff';
      ctx.lineWidth = 2;
      ctx.stroke();
    });
  } else {
    // 2D Rotating Ninja Shuriken (Matching Renderer::drawRotatingShuriken)
    ctx.save();
    ctx.translate(cx, cy);
    ctx.rotate(radZ);
    ctx.scale(scale, scale);

    const shurikenRadius = 80;
    ctx.fillStyle = '#00e5ff';
    ctx.shadowColor = '#00e5ff';
    ctx.shadowBlur = 15;

    for (let blade = 0; blade < 4; blade++) {
      ctx.save();
      ctx.rotate((blade * Math.PI) / 2);
      ctx.beginPath();
      ctx.moveTo(0, -10);
      ctx.lineTo(shurikenRadius, 0);
      ctx.lineTo(0, 10);
      ctx.closePath();
      ctx.fill();
      ctx.restore();
    }
    ctx.shadowBlur = 0;

    // Center Core
    ctx.fillStyle = '#ff2a6d';
    ctx.beginPath();
    ctx.arc(0, 0, 16, 0, Math.PI * 2);
    ctx.fill();
    ctx.strokeStyle = '#fff';
    ctx.lineWidth = 2;
    ctx.stroke();

    ctx.restore();
  }

  const logEl = document.getElementById('cgTransformLog');
  if (logEl) {
    logEl.innerHTML = `<span style="color:#94a3b8;">RotX:</span> ${Math.round(rotX)}&deg; &bull; <span style="color:#94a3b8;">RotY:</span> ${Math.round(rotY)}&deg; &bull; <span style="color:#94a3b8;">RotZ:</span> ${Math.round(rotZ)}&deg; &bull; <span style="color:#00e5ff;">Scale:</span> ${scale.toFixed(1)}x &bull; <span style="color:#00ff88;">Mode:</span> ${objectType.toUpperCase()}`;
  }
}

// -----------------------------------------------------------------------
// 6. PRACTICAL 06: TEXTURES & PHONG LIGHTING (CO6 / TASK 3)
// -----------------------------------------------------------------------
export function drawLightingCanvas() {
  const canvas = document.getElementById('cgCanvasLighting');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  const w = canvas.width;
  const h = canvas.height;

  ctx.fillStyle = '#060913';
  ctx.fillRect(0, 0, w, h);

  const { lightX, lightY, ambient, diffuse, specular, shininess, mode, textureType, materialType } = cgState.lighting;

  // Draw Procedural Texture Grid (Floor or Pillar pattern)
  const tileSize = 40;
  for (let x = 0; x < w; x += tileSize) {
    for (let y = 0; y < h; y += tileSize) {
      if (textureType === 'floor') {
        // High-tech cyber grid
        ctx.strokeStyle = 'rgba(0, 229, 255, 0.12)';
        ctx.lineWidth = 1;
        ctx.strokeRect(x, y, tileSize, tileSize);
        if ((x + y) % (tileSize * 2) === 0) {
          ctx.fillStyle = 'rgba(0, 229, 255, 0.04)';
          ctx.fillRect(x, y, tileSize, tileSize);
        }
      } else if (textureType === 'pillar') {
        ctx.fillStyle = (Math.floor(x / tileSize) % 2 === 0) ? 'rgba(30, 41, 59, 0.4)' : 'rgba(15, 23, 42, 0.6)';
        ctx.fillRect(x, y, tileSize, tileSize);
        ctx.strokeStyle = '#334155';
        ctx.strokeRect(x, y, tileSize, tileSize);
      } else if (textureType === 'shield') {
        // Hexagonal shield cell
        ctx.strokeStyle = 'rgba(0, 255, 136, 0.2)';
        ctx.strokeRect(x, y, tileSize, tileSize);
      }
    }
  }

  // Fighter Character Sphere Demonstration for Phong Shading
  const sphereX = w / 2;
  const sphereY = h / 2;
  const sphereRadius = 110;

  // Light calculation vector
  const lx = lightX - sphereX;
  const ly = lightY - sphereY;
  const lDist = Math.sqrt(lx * lx + ly * ly) || 1;
  const normLx = lx / lDist;
  const normLy = ly / lDist;

  // Material Archetype Color Mapping
  let baseColor = { r: 0, g: 229, b: 255 }; // Cyber Brawler
  if (materialType === 'ninja') baseColor = { r: 140, g: 80, b: 250 };
  if (materialType === 'blaze') baseColor = { r: 255, g: 42, b: 109 };
  if (materialType === 'paladin') baseColor = { r: 255, g: 204, b: 0 };
  if (materialType === 'berserker') baseColor = { r: 239, g: 68, b: 68 };
  if (materialType === 'gladiator') baseColor = { r: 148, g: 163, b: 184 };

  if (mode === 'unlit') {
    // Raw flat unlit
    ctx.fillStyle = `rgb(${baseColor.r}, ${baseColor.g}, ${baseColor.b})`;
    ctx.beginPath();
    ctx.arc(sphereX, sphereY, sphereRadius, 0, Math.PI * 2);
    ctx.fill();
  } else {
    // Phong Illumination Rendering via Radial Gradient Simulation
    // Highlight Center offset by light direction
    const highlightX = sphereX + normLx * (sphereRadius * 0.45);
    const highlightY = sphereY + normLy * (sphereRadius * 0.45);

    const grad = ctx.createRadialGradient(highlightX, highlightY, 5, sphereX, sphereY, sphereRadius);

    // Specular Hotspot
    const specFactor = (mode === 'dynamic') ? specular : 0.5;
    grad.addColorStop(0, `rgba(255, 255, 255, ${specFactor})`);

    // Diffuse Zone
    const diffR = Math.min(255, Math.round(baseColor.r * diffuse));
    const diffG = Math.min(255, Math.round(baseColor.g * diffuse));
    const diffB = Math.min(255, Math.round(baseColor.b * diffuse));
    grad.addColorStop(0.4, `rgb(${diffR}, ${diffG}, ${diffB})`);

    // Ambient Zone
    const ambR = Math.round(baseColor.r * ambient);
    const ambG = Math.round(baseColor.g * ambient);
    const ambB = Math.round(baseColor.b * ambient);
    grad.addColorStop(1, `rgb(${ambR}, ${ambG}, ${ambB})`);

    ctx.fillStyle = grad;
    ctx.beginPath();
    ctx.arc(sphereX, sphereY, sphereRadius, 0, Math.PI * 2);
    ctx.fill();
  }

  // Draw Light Source Gizmo
  ctx.save();
  ctx.fillStyle = '#ffcc00';
  ctx.shadowColor = '#ffcc00';
  ctx.shadowBlur = 20;
  ctx.beginPath();
  ctx.arc(lightX, lightY, 12, 0, Math.PI * 2);
  ctx.fill();

  ctx.strokeStyle = 'rgba(255, 204, 0, 0.5)';
  ctx.lineWidth = 1.5;
  ctx.setLineDash([4, 4]);
  ctx.beginPath();
  ctx.moveTo(lightX, lightY);
  ctx.lineTo(sphereX, sphereY);
  ctx.stroke();
  ctx.restore();

  // Label
  ctx.fillStyle = '#fff';
  ctx.font = 'bold 12px "Rajdhani", sans-serif';
  ctx.fillText('Dynamic Combat Point Light [LIGHT1]', lightX + 16, lightY + 4);

  const logEl = document.getElementById('cgLightingLog');
  if (logEl) {
    logEl.innerHTML = `<span style="color:#94a3b8;">Lighting Mode:</span> <strong style="color:var(--neon-cyan);">${mode.toUpperCase()}</strong> &bull; <span style="color:#94a3b8;">Ambient:</span> ${ambient.toFixed(2)} &bull; <span style="color:#94a3b8;">Diffuse:</span> ${diffuse.toFixed(2)} &bull; <span style="color:#94a3b8;">Specular:</span> ${specular.toFixed(2)} &bull; <span style="color:#94a3b8;">Shininess s:</span> ${shininess}`;
  }
}

// -----------------------------------------------------------------------
// 7. TASK 4: AABB COLLISION GEOMETRY (CO4 / TASK 4)
// -----------------------------------------------------------------------
export function drawCollisionCanvas() {
  const canvas = document.getElementById('cgCanvasCollision');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  const w = canvas.width;
  const h = canvas.height;

  ctx.fillStyle = '#060913';
  ctx.fillRect(0, 0, w, h);

  const { box1, box2 } = cgState.collision;

  // Check AABB Overlap Condition
  const overlap = (box1.x < box2.x + box2.w) &&
                  (box1.x + box1.w > box2.x) &&
                  (box1.y < box2.y + box2.h) &&
                  (box1.y + box1.h > box2.y);

  // Draw Box 1 (Player 1 Hurtbox)
  ctx.fillStyle = 'rgba(0, 229, 255, 0.2)';
  ctx.fillRect(box1.x, box1.y, box1.w, box1.h);
  ctx.strokeStyle = '#00e5ff';
  ctx.lineWidth = 2;
  ctx.strokeRect(box1.x, box1.y, box1.w, box1.h);
  ctx.fillStyle = '#00e5ff';
  ctx.font = 'bold 12px "Orbitron", sans-serif';
  ctx.fillText(box1.label, box1.x + 6, box1.y - 8);

  // Draw Box 2 (Player 2 Attack Hitbox)
  ctx.fillStyle = overlap ? 'rgba(255, 42, 109, 0.4)' : 'rgba(255, 42, 109, 0.2)';
  ctx.fillRect(box2.x, box2.y, box2.w, box2.h);
  ctx.strokeStyle = '#ff2a6d';
  ctx.lineWidth = 2;
  ctx.strokeRect(box2.x, box2.y, box2.w, box2.h);
  ctx.fillStyle = '#ff2a6d';
  ctx.fillText(box2.label, box2.x + 6, box2.y - 8);

  // If overlapping, draw collision penetration box and hitsparks!
  if (overlap) {
    const ox1 = Math.max(box1.x, box2.x);
    const oy1 = Math.max(box1.y, box2.y);
    const ox2 = Math.min(box1.x + box1.w, box2.x + box2.w);
    const oy2 = Math.min(box1.y + box1.h, box2.y + box2.h);

    ctx.fillStyle = 'rgba(255, 204, 0, 0.5)';
    ctx.fillRect(ox1, oy1, ox2 - ox1, oy2 - oy1);
    ctx.strokeStyle = '#ffcc00';
    ctx.lineWidth = 2;
    ctx.strokeRect(ox1, oy1, ox2 - ox1, oy2 - oy1);

    // Hitspark burst
    const sparkX = (ox1 + ox2) / 2;
    const sparkY = (oy1 + oy2) / 2;
    ctx.save();
    ctx.strokeStyle = '#ffcc00';
    ctx.shadowColor = '#ffcc00';
    ctx.shadowBlur = 15;
    ctx.lineWidth = 2;
    for (let i = 0; i < 8; i++) {
      const ang = (i * Math.PI) / 4;
      ctx.beginPath();
      ctx.moveTo(sparkX, sparkY);
      ctx.lineTo(sparkX + Math.cos(ang) * 25, sparkY + Math.sin(ang) * 25);
      ctx.stroke();
    }
    ctx.restore();
  }

  const logEl = document.getElementById('cgCollisionLog');
  if (logEl) {
    if (overlap) {
      logEl.innerHTML = '<span class="badge badge-win" style="background:rgba(255,42,109,0.2); color:var(--neon-pink); border-color:var(--neon-pink); font-size:0.9rem;">COLLISION DETECTED &bull; HIT CONFIRMED!</span>';
    } else {
      logEl.innerHTML = '<span class="badge badge-loss" style="font-size:0.9rem;">DISJOINT &bull; NO COLLISION</span>';
    }
  }
}

// -----------------------------------------------------------------------
// 8. MASTER INITIALIZATION & EVENT LISTENERS
// -----------------------------------------------------------------------
let cgLoopRunning = false;
let cgAnimFrameId = null;

export function initCgLab() {
  setupInteractiveCanvasEvents();
  drawClippingCanvas();
  drawBresenhamLineCanvas();
  drawBresenhamCircleCanvas();
  drawScanLineCanvas();
  draw3DTransformCanvas();
  drawLightingCanvas();
  drawCollisionCanvas();

  if (!cgLoopRunning) {
    cgLoopRunning = true;
    function animLoop() {
      if (document.getElementById('view-cg')?.classList.contains('active')) {
        if (cgState.transform3D.autoSpin) draw3DTransformCanvas();
        if (cgState.scanline.autoSweep) drawScanLineCanvas();
        if (cgState.clipping.autoLaserActive) {
          cgState.clipping.inGameLaserAngle += 0.02;
          const sweep = Math.sin(cgState.clipping.inGameLaserAngle) * 120;
          cgState.clipping.p0.y = 350 + sweep;
          cgState.clipping.p1.y = 90 - sweep;
          drawClippingCanvas();
        }
      }
      cgAnimFrameId = requestAnimationFrame(animLoop);
    }
    cgAnimFrameId = requestAnimationFrame(animLoop);
  }
}

function setupInteractiveCanvasEvents() {
  // Cohen-Sutherland Clipping Canvas Dragging
  const clipCanvas = document.getElementById('cgCanvasClipping');
  if (clipCanvas && !clipCanvas._hasEvents) {
    clipCanvas._hasEvents = true;

    function getMousePos(e) {
      const rect = clipCanvas.getBoundingClientRect();
      const scaleX = clipCanvas.width / rect.width;
      const scaleY = clipCanvas.height / rect.height;
      return {
        x: (e.clientX - rect.left) * scaleX,
        y: (e.clientY - rect.top) * scaleY
      };
    }

    clipCanvas.addEventListener('mousedown', (e) => {
      const pos = getMousePos(e);
      const d0 = Math.hypot(pos.x - cgState.clipping.p0.x, pos.y - cgState.clipping.p0.y);
      const d1 = Math.hypot(pos.x - cgState.clipping.p1.x, pos.y - cgState.clipping.p1.y);

      if (d0 < 25) {
        cgState.clipping.draggingPoint = 'p0';
        cgState.clipping.autoLaserActive = false;
      } else if (d1 < 25) {
        cgState.clipping.draggingPoint = 'p1';
        cgState.clipping.autoLaserActive = false;
      }
    });

    window.addEventListener('mousemove', (e) => {
      if (!cgState.clipping.draggingPoint) return;
      const pos = getMousePos(e);
      const pt = cgState.clipping[cgState.clipping.draggingPoint];
      pt.x = Math.max(0, Math.min(clipCanvas.width, pos.x));
      pt.y = Math.max(0, Math.min(clipCanvas.height, pos.y));
      drawClippingCanvas();
    });

    window.addEventListener('mouseup', () => {
      cgState.clipping.draggingPoint = null;
    });
  }

  // Lighting Canvas Interactive Light Drag
  const lightCanvas = document.getElementById('cgCanvasLighting');
  if (lightCanvas && !lightCanvas._hasEvents) {
    lightCanvas._hasEvents = true;

    function getMousePos(e) {
      const rect = lightCanvas.getBoundingClientRect();
      const scaleX = lightCanvas.width / rect.width;
      const scaleY = lightCanvas.height / rect.height;
      return {
        x: (e.clientX - rect.left) * scaleX,
        y: (e.clientY - rect.top) * scaleY
      };
    }

    let draggingLight = false;
    lightCanvas.addEventListener('mousedown', (e) => {
      const pos = getMousePos(e);
      const d = Math.hypot(pos.x - cgState.lighting.lightX, pos.y - cgState.lighting.lightY);
      if (d < 35 || true) { // allow clicking anywhere on light canvas to relocate light
        draggingLight = true;
        cgState.lighting.lightX = pos.x;
        cgState.lighting.lightY = pos.y;
        drawLightingCanvas();
      }
    });

    window.addEventListener('mousemove', (e) => {
      if (!draggingLight) return;
      const pos = getMousePos(e);
      cgState.lighting.lightX = Math.max(10, Math.min(lightCanvas.width - 10, pos.x));
      cgState.lighting.lightY = Math.max(10, Math.min(lightCanvas.height - 10, pos.y));
      drawLightingCanvas();
    });

    window.addEventListener('mouseup', () => {
      draggingLight = false;
    });
  }

  // Collision Canvas Dragging
  const colCanvas = document.getElementById('cgCanvasCollision');
  if (colCanvas && !colCanvas._hasEvents) {
    colCanvas._hasEvents = true;

    function getMousePos(e) {
      const rect = colCanvas.getBoundingClientRect();
      const scaleX = colCanvas.width / rect.width;
      const scaleY = colCanvas.height / rect.height;
      return {
        x: (e.clientX - rect.left) * scaleX,
        y: (e.clientY - rect.top) * scaleY
      };
    }

    colCanvas.addEventListener('mousedown', (e) => {
      const pos = getMousePos(e);
      const b1 = cgState.collision.box1;
      const b2 = cgState.collision.box2;

      if (pos.x >= b2.x && pos.x <= b2.x + b2.w && pos.y >= b2.y && pos.y <= b2.y + b2.h) {
        cgState.collision.draggingBox = 'box2';
        cgState.collision.dragOffset = { x: pos.x - b2.x, y: pos.y - b2.y };
      } else if (pos.x >= b1.x && pos.x <= b1.x + b1.w && pos.y >= b1.y && pos.y <= b1.y + b1.h) {
        cgState.collision.draggingBox = 'box1';
        cgState.collision.dragOffset = { x: pos.x - b1.x, y: pos.y - b1.y };
      }
    });

    window.addEventListener('mousemove', (e) => {
      if (!cgState.collision.draggingBox) return;
      const pos = getMousePos(e);
      const box = cgState.collision[cgState.collision.draggingBox];
      box.x = pos.x - cgState.collision.dragOffset.x;
      box.y = pos.y - cgState.collision.dragOffset.y;
      drawCollisionCanvas();
    });

    window.addEventListener('mouseup', () => {
      cgState.collision.draggingBox = null;
    });
  }
}

// -----------------------------------------------------------------------
// 9. GLOBAL UI ACTION HELPERS
// -----------------------------------------------------------------------
if (typeof window !== 'undefined') {
  window.filterCgTasks = function(taskFilter) {
    document.querySelectorAll('.cg-task-btn').forEach(b => b.classList.remove('active'));
    const btn = document.getElementById('btn-cg-task-' + taskFilter);
    if (btn) btn.classList.add('active');

    const cards = document.querySelectorAll('.cg-prac-card');
    cards.forEach(card => {
      if (taskFilter === 'all' || card.getAttribute('data-task') === String(taskFilter)) {
        card.style.display = 'block';
      } else {
        card.style.display = 'none';
      }
    });
  };

  window.scrollToCgPractical = function(pracId) {
    window.switchView('cg');
    setTimeout(() => {
      const el = document.getElementById(pracId);
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'start' });
        el.style.boxShadow = '0 0 35px var(--neon-cyan)';
        setTimeout(() => { el.style.boxShadow = ''; }, 2000);
      }
    }, 100);
  };

  window.setClippingPreset = function(presetName) {
    cgState.clipping.autoLaserActive = false;
    if (presetName === 'diagonal') {
      cgState.clipping.p0 = { x: 80, y: 390 };
      cgState.clipping.p1 = { x: 780, y: 60 };
    } else if (presetName === 'inside') {
      cgState.clipping.p0 = { x: 260, y: 160 };
      cgState.clipping.p1 = { x: 600, y: 280 };
    } else if (presetName === 'outside') {
      cgState.clipping.p0 = { x: 30, y: 40 };
      cgState.clipping.p1 = { x: 140, y: 80 };
    } else if (presetName === 'corner') {
      cgState.clipping.p0 = { x: 500, y: 220 };
      cgState.clipping.p1 = { x: 780, y: 380 };
    } else if (presetName === 'laser') {
      cgState.clipping.autoLaserActive = true;
    }
    drawClippingCanvas();
  };

  window.setBresenhamLinePreset = function(presetName) {
    if (presetName === 'shallow') {
      cgState.bresenhamLine.x1 = 2; cgState.bresenhamLine.y1 = 2;
      cgState.bresenhamLine.x2 = 26; cgState.bresenhamLine.y2 = 8;
    } else if (presetName === 'steep') {
      cgState.bresenhamLine.x1 = 4; cgState.bresenhamLine.y1 = 2;
      cgState.bresenhamLine.x2 = 10; cgState.bresenhamLine.y2 = 18;
    } else if (presetName === 'negative') {
      cgState.bresenhamLine.x1 = 2; cgState.bresenhamLine.y1 = 18;
      cgState.bresenhamLine.x2 = 24; cgState.bresenhamLine.y2 = 3;
    } else if (presetName === 'horizontal') {
      cgState.bresenhamLine.x1 = 2; cgState.bresenhamLine.y1 = 10;
      cgState.bresenhamLine.x2 = 26; cgState.bresenhamLine.y2 = 10;
    }
    drawBresenhamLineCanvas();
  };

  window.setScanlineShape = function(shapeName) {
    cgState.scanline.shape = shapeName;
    cgState.scanline.sweepY = null;
    drawScanLineCanvas();
  };

  window.updateScanlineHP = function(val) {
    cgState.scanline.hpPercent = parseInt(val);
    drawScanLineCanvas();
  };

  window.toggleScanlineAutoSweep = function() {
    cgState.scanline.autoSweep = !cgState.scanline.autoSweep;
  };

  window.set3DObjectType = function(type) {
    cgState.transform3D.objectType = type;
    draw3DTransformCanvas();
  };

  window.toggle3DAutoSpin = function() {
    cgState.transform3D.autoSpin = !cgState.transform3D.autoSpin;
    draw3DTransformCanvas();
  };

  window.setLightingMode = function(mode) {
    cgState.lighting.mode = mode;
    drawLightingCanvas();
  };

  window.setTextureType = function(tex) {
    cgState.lighting.textureType = tex;
    drawLightingCanvas();
  };

  window.setMaterialPreset = function(mat) {
    cgState.lighting.materialType = mat;
    if (mat === 'cyber') { cgState.lighting.shininess = 96; cgState.lighting.specular = 0.95; }
    if (mat === 'ninja') { cgState.lighting.shininess = 24; cgState.lighting.specular = 0.4; }
    if (mat === 'blaze') { cgState.lighting.shininess = 64; cgState.lighting.specular = 0.85; }
    if (mat === 'paladin') { cgState.lighting.shininess = 128; cgState.lighting.specular = 1.0; }
    if (mat === 'berserker') { cgState.lighting.shininess = 40; cgState.lighting.specular = 0.7; }
    if (mat === 'gladiator') { cgState.lighting.shininess = 48; cgState.lighting.specular = 0.6; }
    drawLightingCanvas();
  };

  window.copyCppSource = function(buttonEl, codeId) {
    const codeEl = document.getElementById(codeId);
    if (!codeEl) return;
    navigator.clipboard.writeText(codeEl.innerText).then(() => {
      const originalText = buttonEl.innerText;
      buttonEl.innerText = 'COPIED TO CLIPBOARD!';
      buttonEl.style.background = '#00ff88';
      buttonEl.style.color = '#000';
      setTimeout(() => {
        buttonEl.innerText = originalText;
        buttonEl.style.background = '';
        buttonEl.style.color = '';
      }, 1500);
    });
  };
}
