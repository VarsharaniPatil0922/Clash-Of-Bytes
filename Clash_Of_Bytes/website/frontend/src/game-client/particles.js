// =======================================================================
// CLASH OF BYTES: 2D COMBAT PARTICLE & VFX SYSTEM
// Simulates spark explosions, energy impact rings, and Gouraud-glow trails
// =======================================================================

export class ParticleSystem {
  constructor() {
    this.particles = [];
  }

  emitHitSparks(x, y, color = '#00e5ff', count = 16) {
    for (let i = 0; i < count; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = 2 + Math.random() * 6;
      this.particles.push({
        x,
        y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
        radius: 2 + Math.random() * 3,
        color,
        alpha: 1.0,
        decay: 0.03 + Math.random() * 0.04,
        gravity: 0.15,
      });
    }
  }

  emitBlockSparks(x, y) {
    for (let i = 0; i < 12; i++) {
      const angle = Math.PI + (Math.random() - 0.5) * 1.5;
      const speed = 1.5 + Math.random() * 4;
      this.particles.push({
        x,
        y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed - 1,
        radius: 2.5,
        color: '#ffcc00', // Gold
        alpha: 1.0,
        decay: 0.05,
        gravity: 0.1,
      });
    }
  }

  emitSpecialBurst(x, y, color) {
    for (let i = 0; i < 28; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = 4 + Math.random() * 8;
      this.particles.push({
        x,
        y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
        radius: 3 + Math.random() * 4,
        color,
        alpha: 1.0,
        decay: 0.025,
        gravity: 0.05,
      });
    }
  }

  update() {
    for (let i = this.particles.length - 1; i >= 0; i--) {
      const p = this.particles[i];
      p.x += p.vx;
      p.y += p.vy;
      p.vy += p.gravity;
      p.alpha -= p.decay;

      if (p.alpha <= 0) {
        this.particles.splice(i, 1);
      }
    }
  }

  draw(ctx) {
    ctx.save();
    ctx.globalCompositeOperation = 'lighter';
    for (const p of this.particles) {
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
      ctx.fillStyle = p.color;
      ctx.globalAlpha = Math.max(0, p.alpha);
      ctx.shadowBlur = 8;
      ctx.shadowColor = p.color;
      ctx.fill();
    }
    ctx.restore();
  }

  clear() {
    this.particles = [];
  }
}
