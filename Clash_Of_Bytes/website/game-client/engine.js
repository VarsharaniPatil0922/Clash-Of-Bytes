// =======================================================================
// CLASH OF BYTES: 2D COMBAT GAME ENGINE
// 60 FPS Fixed-Timestep Loop, AABB Collision, HUD & Match Persistence
// =======================================================================

import { ARENA, INPUT_MAPPINGS } from './constants.js';
import { Fighter } from './fighter.js';
import { ParticleSystem } from './particles.js';
import { sounds } from './audio.js';

export class ClashGameEngine {
  constructor(canvas, p1Config = { id: 4, name: 'Cyber Brawler' }, p2Config = { id: 3, name: 'Shadow Ninja' }, onMatchEnd = null) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.onMatchEnd = onMatchEnd;

    this.p1CharId = p1Config.id;
    this.p2CharId = p2Config.id;
    this.p1Name = p1Config.name || 'Player 1';
    this.p2Name = p2Config.name || 'Player 2';

    // Entities
    this.p1 = new Fighter(1, this.p1CharId, 220, true);
    this.p2 = new Fighter(2, this.p2CharId, ARENA.WIDTH - 220, false);
    this.particles = new ParticleSystem();

    // Round Management
    this.roundNumber = 1;
    this.roundTimer = ARENA.ROUND_TIME_SECONDS;
    this.roundState = 'INTRO'; // 'INTRO', 'FIGHTING', 'ROUND_OVER', 'MATCH_OVER'
    this.roundStateTimer = 2.0; // 2 seconds intro
    this.roundWinner = null;
    this.matchWinner = null;
    this.roundHistory = [];

    // Keys State
    this.activeKeys = new Set();
    this.p1Input = { left: false, right: false, jump: false, block: false, lightAttack: false, heavyAttack: false, special: false };
    this.p2Input = { left: false, right: false, jump: false, block: false, lightAttack: false, heavyAttack: false, special: false };

    // Loop
    this.isRunning = false;
    this.lastTime = 0;
    this.boundLoop = this.loop.bind(this);
    this.boundKeyDown = this.handleKeyDown.bind(this);
    this.boundKeyUp = this.handleKeyUp.bind(this);
  }

  start() {
    this.canvas.width = ARENA.WIDTH;
    this.canvas.height = ARENA.HEIGHT;
    this.isRunning = true;
    this.lastTime = performance.now();

    window.addEventListener('keydown', this.boundKeyDown);
    window.addEventListener('keyup', this.boundKeyUp);

    sounds.init();
    sounds.playRoundStart();

    requestAnimationFrame(this.boundLoop);
  }

  stop() {
    this.isRunning = false;
    window.removeEventListener('keydown', this.boundKeyDown);
    window.removeEventListener('keyup', this.boundKeyUp);
  }

  handleKeyDown(e) {
    sounds.init();
    this.activeKeys.add(e.code);
    this.updateInputs();

    // Prevent default scrolling for fighting keys
    if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', 'Space', 'KeyW', 'KeyS'].includes(e.code)) {
      e.preventDefault();
    }
  }

  handleKeyUp(e) {
    this.activeKeys.delete(e.code);
    this.updateInputs();
  }

  updateInputs() {
    const k = this.activeKeys;
    const m1 = INPUT_MAPPINGS.PLAYER_1;
    const m2 = INPUT_MAPPINGS.PLAYER_2;

    this.p1Input = {
      left: m1.LEFT.some(c => k.has(c)),
      right: m1.RIGHT.some(c => k.has(c)),
      jump: m1.JUMP.some(c => k.has(c)),
      block: m1.BLOCK.some(c => k.has(c)),
      lightAttack: m1.LIGHT_ATTACK.some(c => k.has(c)),
      heavyAttack: m1.HEAVY_ATTACK.some(c => k.has(c)),
      special: m1.SPECIAL.some(c => k.has(c)),
    };

    this.p2Input = {
      left: m2.LEFT.some(c => k.has(c)),
      right: m2.RIGHT.some(c => k.has(c)),
      jump: m2.JUMP.some(c => k.has(c)),
      block: m2.BLOCK.some(c => k.has(c)),
      lightAttack: m2.LIGHT_ATTACK.some(c => k.has(c)),
      heavyAttack: m2.HEAVY_ATTACK.some(c => k.has(c)),
      special: m2.SPECIAL.some(c => k.has(c)),
    };
  }

  loop(currentTime) {
    if (!this.isRunning) return;

    const dt = Math.min((currentTime - this.lastTime) / 1000, 0.05); // Cap to 50ms to prevent spiral
    this.lastTime = currentTime;

    this.update(dt);
    this.render();

    requestAnimationFrame(this.boundLoop);
  }

  update(dt) {
    this.particles.update();

    if (this.roundState === 'INTRO') {
      this.roundStateTimer -= dt;
      if (this.roundStateTimer <= 0) {
        this.roundState = 'FIGHTING';
      }
      return;
    }

    if (this.roundState === 'ROUND_OVER') {
      this.roundStateTimer -= dt;
      if (this.roundStateTimer <= 0) {
        this.prepareNextRound();
      }
      return;
    }

    if (this.roundState === 'MATCH_OVER') {
      return;
    }

    // FIGHTING STATE
    this.roundTimer = Math.max(0, this.roundTimer - dt);

    // Update Entities
    this.p1.update(dt, this.p2, this.p1Input, this.particles, sounds);
    this.p2.update(dt, this.p1, this.p2Input, this.particles, sounds);

    // Collision Detection: P1 attack against P2 hurtbox
    this.checkCollisions(this.p1, this.p2);
    this.checkCollisions(this.p2, this.p1);

    // Check Round Termination Conditions (HP <= 0 or Timer == 0)
    if (this.p1.hp <= 0 || this.p2.hp <= 0 || this.roundTimer <= 0) {
      this.finishRound();
    }
  }

  checkCollisions(attacker, defender) {
    const hitbox = attacker.getHitbox();
    if (!hitbox || attacker.hasHitOpponent) return;

    const hurtbox = defender.getHurtbox();

    // AABB Intersection Check
    if (
      hitbox.x < hurtbox.x + hurtbox.w &&
      hitbox.x + hitbox.w > hurtbox.x &&
      hitbox.y < hurtbox.y + hurtbox.h &&
      hitbox.y + hitbox.h > hurtbox.y
    ) {
      attacker.hasHitOpponent = true;
      defender.takeDamage(hitbox, attacker, this.particles, sounds);
    }
  }

  finishRound() {
    this.roundState = 'ROUND_OVER';
    this.roundStateTimer = 3.0; // 3 seconds victory screen

    let winnerNum = 0;
    if (this.p1.hp > this.p2.hp) {
      winnerNum = 1;
      this.p1.roundsWon++;
      this.roundWinner = this.p1;
    } else if (this.p2.hp > this.p1.hp) {
      winnerNum = 2;
      this.p2.roundsWon++;
      this.roundWinner = this.p2;
    } else {
      this.roundWinner = null; // Draw
    }

    // Save Round Telemetry
    this.roundHistory.push({
      roundNumber: this.roundNumber,
      winnerPlayerIndex: winnerNum,
      p1Score: this.p1.score,
      p2Score: this.p2.score,
    });

    // Check Match Win Condition
    if (this.p1.roundsWon >= ARENA.ROUNDS_TO_WIN) {
      this.matchWinner = 1;
      this.roundState = 'MATCH_OVER';
      sounds.playVictory();
      this.triggerMatchCompletion();
    } else if (this.p2.roundsWon >= ARENA.ROUNDS_TO_WIN) {
      this.matchWinner = 2;
      this.roundState = 'MATCH_OVER';
      sounds.playVictory();
      this.triggerMatchCompletion();
    }
  }

  prepareNextRound() {
    this.roundNumber++;
    this.roundTimer = ARENA.ROUND_TIME_SECONDS;
    this.roundState = 'INTRO';
    this.roundStateTimer = 2.0;
    this.roundWinner = null;

    this.p1.resetForRound(220, true);
    this.p2.resetForRound(ARENA.WIDTH - 220, false);
    this.particles.clear();

    sounds.playRoundStart();
  }

  triggerMatchCompletion() {
    if (this.onMatchEnd) {
      const payload = {
        winnerPlayerIndex: this.matchWinner,
        p1Score: this.p1.score,
        p2Score: this.p2.score,
        totalRounds: this.roundNumber,
        rounds: this.roundHistory,
      };
      this.onMatchEnd(payload);
    }
  }

  render() {
    const ctx = this.ctx;
    const w = ARENA.WIDTH;
    const h = ARENA.HEIGHT;

    ctx.clearRect(0, 0, w, h);

    // 1. Cyberpunk Arena Backdrop
    this.renderArena(ctx, w, h);

    // 2. Entities
    this.p1.draw(ctx);
    this.p2.draw(ctx);

    // 3. VFX & Particles
    this.particles.draw(ctx);

    // 4. Fighting HUD
    this.renderHUD(ctx, w, h);

    // 5. Round State Banners
    this.renderBanners(ctx, w, h);
  }

  renderArena(ctx, w, h) {
    // Cyber Sky Gradient
    const skyGrad = ctx.createLinearGradient(0, 0, 0, ARENA.GROUND_Y);
    skyGrad.addColorStop(0, '#040711');
    skyGrad.addColorStop(0.65, '#0b1329');
    skyGrad.addColorStop(1, '#1e1b4b');
    ctx.fillStyle = skyGrad;
    ctx.fillRect(0, 0, w, ARENA.GROUND_Y);

    // Neon Skyline Silhouettes
    ctx.fillStyle = '#090d1a';
    ctx.fillRect(80, 160, 90, 280);
    ctx.fillRect(200, 220, 130, 220);
    ctx.fillRect(360, 140, 110, 300);
    ctx.fillRect(520, 180, 140, 260);
    ctx.fillRect(700, 120, 120, 320);
    ctx.fillRect(840, 240, 80, 200);

    // Skyline Neon Window Accents
    ctx.fillStyle = 'rgba(0, 229, 255, 0.25)';
    for (let bx = 90; bx < 880; bx += 70) {
      for (let by = 200; by < 400; by += 45) {
        if ((bx + by) % 3 === 0) {
          ctx.fillRect(bx, by, 8, 14);
        }
      }
    }

    // Floor Platform
    const floorGrad = ctx.createLinearGradient(0, ARENA.GROUND_Y, 0, h);
    floorGrad.addColorStop(0, '#0f172a');
    floorGrad.addColorStop(1, '#020617');
    ctx.fillStyle = floorGrad;
    ctx.fillRect(0, ARENA.GROUND_Y, w, h - ARENA.GROUND_Y);

    // Laser Floor Divider
    ctx.strokeStyle = '#00e5ff';
    ctx.lineWidth = 2.5;
    ctx.shadowBlur = 10;
    ctx.shadowColor = '#00e5ff';
    ctx.beginPath();
    ctx.moveTo(0, ARENA.GROUND_Y);
    ctx.lineTo(w, ARENA.GROUND_Y);
    ctx.stroke();

    // Perspective Cyber Grid Lines on Floor
    ctx.strokeStyle = 'rgba(0, 229, 255, 0.15)';
    ctx.lineWidth = 1;
    ctx.shadowBlur = 0;
    for (let x = 0; x <= w; x += 60) {
      ctx.beginPath();
      ctx.moveTo(x, ARENA.GROUND_Y);
      ctx.lineTo(x + (x - w / 2) * 0.7, h);
      ctx.stroke();
    }
  }

  renderHUD(ctx, w, h) {
    ctx.save();

    // Player 1 Health Bar (Left to Right)
    const barWidth = 340;
    const barHeight = 24;
    const topY = 28;

    // P1 Health Background
    ctx.fillStyle = 'rgba(15, 23, 42, 0.8)';
    ctx.fillRect(50, topY, barWidth, barHeight);
    ctx.strokeStyle = '#334155';
    ctx.lineWidth = 2;
    ctx.strokeRect(50, topY, barWidth, barHeight);

    // P1 Health Fill (Gouraud gradient)
    const p1HpRatio = Math.max(0, this.p1.hp / this.p1.maxHp);
    const p1Grad = ctx.createLinearGradient(50, topY, 50 + barWidth, topY);
    p1Grad.addColorStop(0, '#ff2a6d');
    p1Grad.addColorStop(0.5, '#ffcc00');
    p1Grad.addColorStop(1, '#00ff88');
    ctx.fillStyle = p1Grad;
    ctx.fillRect(50, topY, barWidth * p1HpRatio, barHeight);

    // Player 2 Health Bar (Right to Left)
    const p2StartX = w - 50 - barWidth;
    ctx.fillStyle = 'rgba(15, 23, 42, 0.8)';
    ctx.fillRect(p2StartX, topY, barWidth, barHeight);
    ctx.strokeStyle = '#334155';
    ctx.strokeRect(p2StartX, topY, barWidth, barHeight);

    const p2HpRatio = Math.max(0, this.p2.hp / this.p2.maxHp);
    const p2Grad = ctx.createLinearGradient(p2StartX + barWidth, topY, p2StartX, topY);
    p2Grad.addColorStop(0, '#ff2a6d');
    p2Grad.addColorStop(0.5, '#ffcc00');
    p2Grad.addColorStop(1, '#00ff88');
    ctx.fillStyle = p2Grad;
    ctx.fillRect(p2StartX + barWidth * (1 - p2HpRatio), topY, barWidth * p2HpRatio, barHeight);

    // Center Timer Box
    ctx.fillStyle = '#0f172a';
    ctx.fillRect(w / 2 - 40, topY - 10, 80, 48);
    ctx.strokeStyle = '#00e5ff';
    ctx.lineWidth = 2;
    ctx.strokeRect(w / 2 - 40, topY - 10, 80, 48);

    ctx.font = 'bold 26px sans-serif';
    ctx.textAlign = 'center';
    ctx.fillStyle = (this.roundTimer <= 10) ? '#ff2a6d' : '#ffffff';
    ctx.fillText(Math.ceil(this.roundTimer).toString(), w / 2, topY + 24);

    // Names & Archetypes
    ctx.font = 'bold 16px sans-serif';
    ctx.textAlign = 'left';
    ctx.fillStyle = '#00e5ff';
    ctx.fillText(`${this.p1Name} (${this.p1.name})`, 50, topY - 8);

    ctx.textAlign = 'right';
    ctx.fillStyle = '#ff2a6d';
    ctx.fillText(`${this.p2Name} (${this.p2.name})`, w - 50, topY - 8);

    // Round Win Markers
    for (let r = 0; r < ARENA.ROUNDS_TO_WIN; r++) {
      // P1 Round Icons
      ctx.beginPath();
      ctx.arc(50 + r * 22, topY + barHeight + 14, 6, 0, Math.PI * 2);
      ctx.fillStyle = (r < this.p1.roundsWon) ? '#ffcc00' : '#1e293b';
      ctx.fill();
      ctx.strokeStyle = '#ffcc00';
      ctx.lineWidth = 1.5;
      ctx.stroke();

      // P2 Round Icons
      ctx.beginPath();
      ctx.arc(w - 50 - r * 22, topY + barHeight + 14, 6, 0, Math.PI * 2);
      ctx.fillStyle = (r < this.p2.roundsWon) ? '#ffcc00' : '#1e293b';
      ctx.fill();
      ctx.strokeStyle = '#ffcc00';
      ctx.lineWidth = 1.5;
      ctx.stroke();
    }

    // Scores
    ctx.font = '13px sans-serif';
    ctx.textAlign = 'left';
    ctx.fillStyle = '#94a3b8';
    ctx.fillText(`SCORE: ${this.p1.score}`, 120, topY + barHeight + 18);

    ctx.textAlign = 'right';
    ctx.fillText(`SCORE: ${this.p2.score}`, w - 120, topY + barHeight + 18);

    ctx.restore();
  }

  renderBanners(ctx, w, h) {
    ctx.save();
    ctx.textAlign = 'center';

    if (this.roundState === 'INTRO') {
      ctx.font = 'bold 44px sans-serif';
      ctx.fillStyle = '#00e5ff';
      ctx.shadowBlur = 20;
      ctx.shadowColor = '#00e5ff';
      ctx.fillText(`ROUND ${this.roundNumber}`, w / 2, h / 2 - 20);

      ctx.font = 'bold 32px sans-serif';
      ctx.fillStyle = '#ffcc00';
      ctx.shadowColor = '#ffcc00';
      ctx.fillText('READY... FIGHT!', w / 2, h / 2 + 30);
    } else if (this.roundState === 'ROUND_OVER') {
      ctx.font = 'bold 50px sans-serif';
      ctx.fillStyle = '#ff2a6d';
      ctx.shadowBlur = 25;
      ctx.shadowColor = '#ff2a6d';
      ctx.fillText('K. O. !', w / 2, h / 2 - 20);

      const winName = this.roundWinner ? (this.roundWinner.playerIndex === 1 ? this.p1Name : this.p2Name) : 'DRAW';
      ctx.font = 'bold 24px sans-serif';
      ctx.fillStyle = '#ffffff';
      ctx.fillText(`${winName} WINS ROUND ${this.roundNumber}`, w / 2, h / 2 + 30);
    } else if (this.roundState === 'MATCH_OVER') {
      ctx.font = 'bold 52px sans-serif';
      ctx.fillStyle = '#ffcc00';
      ctx.shadowBlur = 30;
      ctx.shadowColor = '#ffcc00';
      ctx.fillText('VICTORY!', w / 2, h / 2 - 20);

      const champ = this.matchWinner === 1 ? this.p1Name : this.p2Name;
      ctx.font = 'bold 28px sans-serif';
      ctx.fillStyle = '#00e5ff';
      ctx.fillText(`${champ} IS THE CLASH CHAMPION!`, w / 2, h / 2 + 30);
    }

    ctx.restore();
  }
}
