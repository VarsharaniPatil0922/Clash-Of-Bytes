// =======================================================================
// CLASH OF BYTES: 2D FIGHTER ENTITY CLASS
// Implements physics, collision hitboxes, state machine, and animation
// Enhanced with cyber armor rendering, damage lag easing, and forcefields
// =======================================================================

import { ARENA, CHARACTERS } from './constants.js';

export const FighterState = {
  IDLE: 'IDLE',
  WALKING: 'WALKING',
  JUMPING: 'JUMPING',
  BLOCKING: 'BLOCKING',
  LIGHT_ATTACK: 'LIGHT_ATTACK',
  HEAVY_ATTACK: 'HEAVY_ATTACK',
  SPECIAL: 'SPECIAL',
  HITSTUN: 'HITSTUN',
  KNOCKED_OUT: 'KNOCKED_OUT',
};

export class Fighter {
  constructor(playerIndex, characterId, startX, faceRight = true) {
    this.playerIndex = playerIndex;
    this.charConfig = CHARACTERS[characterId] || CHARACTERS[1];
    this.characterId = this.charConfig.id;
    this.name = this.charConfig.name;

    this.maxHp = this.charConfig.baseHp;
    this.hp = this.maxHp;
    this.lagHp = this.maxHp; // Delayed catch-up HP for arcade health bar trail
    this.baseAttack = this.charConfig.baseAttack;
    this.baseDefense = this.charConfig.baseDefense;
    this.speed = this.charConfig.speed;
    this.color = this.charConfig.color;
    this.accentColor = this.charConfig.accentColor;
    this.width = this.charConfig.width;
    this.height = this.charConfig.height;

    this.x = startX;
    this.y = ARENA.GROUND_Y - this.height;
    this.vx = 0;
    this.vy = 0;
    this.facing = faceRight ? 1 : -1;
    this.grounded = true;

    this.state = FighterState.IDLE;
    this.stateTimer = 0;
    this.specialCooldownTimer = 0;
    this.comboCounter = 0;
    this.comboTimer = 0;
    this.score = 0;
    this.roundsWon = 0;

    this.currentHitbox = null;
    this.hasHitOpponent = false;
  }

  resetForRound(startX, faceRight) {
    this.x = startX;
    this.y = ARENA.GROUND_Y - this.height;
    this.vx = 0;
    this.vy = 0;
    this.hp = this.maxHp;
    this.lagHp = this.maxHp;
    this.facing = faceRight ? 1 : -1;
    this.grounded = true;
    this.state = FighterState.IDLE;
    this.stateTimer = 0;
    this.currentHitbox = null;
    this.hasHitOpponent = false;
  }

  getHurtbox() {
    return {
      x: this.x - this.width / 2,
      y: this.y,
      w: this.width,
      h: this.height,
    };
  }

  getHitbox() {
    return this.currentHitbox;
  }

  update(dt, opponent, input, particleSystem, soundSystem) {
    // Easing lagHp for arcade health bar damage trail
    if (this.lagHp > this.hp) {
      this.lagHp = Math.max(this.hp, this.lagHp - (this.maxHp * 0.4 * dt));
    }

    if (this.comboTimer > 0) {
      this.comboTimer -= dt;
      if (this.comboTimer <= 0) {
        this.comboCounter = 0;
      }
    }

    if (this.specialCooldownTimer > 0) {
      this.specialCooldownTimer -= dt;
    }

    if (this.state === FighterState.IDLE || this.state === FighterState.WALKING) {
      this.facing = opponent.x > this.x ? 1 : -1;
    }

    if (this.stateTimer > 0) {
      this.stateTimer -= dt;
      if (this.stateTimer <= 0) {
        this.onStateTimeout();
      }
    }

    this.handleInput(input, soundSystem);

    this.x += this.vx;
    this.y += this.vy;

    if (!this.grounded) {
      this.vy += ARENA.GRAVITY;
      if (this.y >= ARENA.GROUND_Y - this.height) {
        this.y = ARENA.GROUND_Y - this.height;
        this.vy = 0;
        this.grounded = true;
        if (this.state === FighterState.JUMPING) {
          this.state = FighterState.IDLE;
        }
      }
    }

    // Arena boundary clamp
    const halfW = this.width / 2;
    if (this.x - halfW < 40) {
      this.x = 40 + halfW;
      this.vx = 0;
    }
    if (this.x + halfW > ARENA.WIDTH - 40) {
      this.x = ARENA.WIDTH - 40 - halfW;
      this.vx = 0;
    }

    this.updateAttackHitbox();
  }

  handleInput(input, soundSystem) {
    if (this.state === FighterState.HITSTUN || 
        this.state === FighterState.KNOCKED_OUT ||
        this.state === FighterState.LIGHT_ATTACK ||
        this.state === FighterState.HEAVY_ATTACK ||
        this.state === FighterState.SPECIAL) {
      return;
    }

    const isDefending = input.defence || input.block;
    if (isDefending && this.grounded) {
      this.state = FighterState.BLOCKING;
      this.vx = 0;
      return;
    } else if (this.state === FighterState.BLOCKING && !isDefending) {
      this.state = FighterState.IDLE;
    }

    if (input.jump && this.grounded) {
      this.vy = -14.5;
      this.grounded = false;
      this.state = FighterState.JUMPING;
    }

    if (input.left) {
      this.vx = -this.speed;
      if (this.grounded && this.state !== FighterState.JUMPING) {
        this.state = FighterState.WALKING;
      }
    } else if (input.right) {
      this.vx = this.speed;
      if (this.grounded && this.state !== FighterState.JUMPING) {
        this.state = FighterState.WALKING;
      }
    } else {
      this.vx = 0;
      if (this.grounded && this.state === FighterState.WALKING) {
        this.state = FighterState.IDLE;
      }
    }

    if (input.attack || input.lightAttack || input.heavyAttack) {
      this.triggerAttack(soundSystem);
    }
  }

  triggerAttack(soundSystem) {
    if (this.comboCounter % 2 === 0) {
      this.triggerLightAttack(soundSystem);
    } else {
      this.triggerHeavyAttack(soundSystem);
    }
  }

  triggerLightAttack(soundSystem) {
    this.state = FighterState.LIGHT_ATTACK;
    this.stateTimer = 0.28;
    this.hasHitOpponent = false;
    this.vx *= 0.3;
    soundSystem.playPunch();
  }

  triggerHeavyAttack(soundSystem) {
    this.state = FighterState.HEAVY_ATTACK;
    this.stateTimer = 0.45;
    this.hasHitOpponent = false;
    this.vx *= 0.1;
    soundSystem.playHeavySlash();
  }

  triggerSpecial(soundSystem) {
    if (this.specialCooldownTimer > 0) return;
    this.state = FighterState.SPECIAL;
    this.stateTimer = 0.65;
    this.specialCooldownTimer = this.charConfig.specialCooldown;
    this.hasHitOpponent = false;
    this.vx = this.facing * 3;
    soundSystem.playSpecial();
  }

  updateAttackHitbox() {
    if (this.hasHitOpponent) {
      this.currentHitbox = null;
      return;
    }

    if (this.state === FighterState.LIGHT_ATTACK && this.stateTimer < 0.22 && this.stateTimer > 0.08) {
      this.currentHitbox = {
        x: this.x + (this.facing > 0 ? this.width / 2 : -this.width / 2 - 45),
        y: this.y + 25,
        w: 45,
        h: 30,
        damage: this.baseAttack * 0.7,
        knockback: 4.0,
        type: 'LIGHT',
      };
    } else if (this.state === FighterState.HEAVY_ATTACK && this.stateTimer < 0.32 && this.stateTimer > 0.1) {
      this.currentHitbox = {
        x: this.x + (this.facing > 0 ? this.width / 2 : -this.width / 2 - 65),
        y: this.y + 15,
        w: 65,
        h: 45,
        damage: this.baseAttack * 1.35,
        knockback: 8.5,
        type: 'HEAVY',
      };
    } else if (this.state === FighterState.SPECIAL && this.stateTimer < 0.5 && this.stateTimer > 0.15) {
      this.currentHitbox = {
        x: this.x + (this.facing > 0 ? this.width / 2 : -this.width / 2 - 80),
        y: this.y + 10,
        w: 80,
        h: 70,
        damage: this.baseAttack * 2.1,
        knockback: 12.0,
        type: 'SPECIAL',
      };
    } else {
      this.currentHitbox = null;
    }
  }

  takeDamage(hitbox, attacker, particleSystem, soundSystem) {
    if (this.state === FighterState.KNOCKED_OUT) return;

    let incomingDmg = hitbox.damage;
    const isBlocking = (this.state === FighterState.BLOCKING);

    if (isBlocking) {
      const damageReduction = Math.min(0.85, (this.baseDefense * 0.08) + 0.5);
      incomingDmg *= (1.0 - damageReduction);
      this.vx = attacker.facing * (hitbox.knockback * 0.4);
      particleSystem.emitBlockSparks(this.x, this.y + 35);
      soundSystem.playBlock();
    } else {
      incomingDmg = Math.max(2.0, incomingDmg - (this.baseDefense * 0.35));
      this.vx = attacker.facing * hitbox.knockback;
      this.vy = -3.5;
      this.grounded = false;
      this.state = FighterState.HITSTUN;
      this.stateTimer = 0.32;
      attacker.comboCounter++;
      attacker.comboTimer = 2.0;
      attacker.score += Math.round(incomingDmg * 10);
      particleSystem.emitHitSparks(this.x, this.y + 35, attacker.color, hitbox.type === 'SPECIAL' ? 30 : 18);
    }

    this.hp = Math.max(0, this.hp - incomingDmg);

    if (this.hp <= 0) {
      this.state = FighterState.KNOCKED_OUT;
      this.stateTimer = 999;
      this.vx = attacker.facing * 8;
      this.vy = -6;
      attacker.score += 500;
    }
  }

  onStateTimeout() {
    if (this.state !== FighterState.KNOCKED_OUT) {
      this.state = FighterState.IDLE;
      this.currentHitbox = null;
      this.hasHitOpponent = false;
    }
  }

  draw(ctx) {
    ctx.save();
    const centerX = this.x;
    const isP1 = (this.playerIndex === 1);
    const facing = this.facing;
    const hurtbox = this.getHurtbox();

    // 1. Dynamic Contact Ground Shadow
    const shadowW = this.state === FighterState.KNOCKED_OUT ? this.height * 0.9 : this.width * 0.85;
    ctx.beginPath();
    ctx.ellipse(centerX, ARENA.GROUND_Y - 4, shadowW / 2, 7, 0, 0, Math.PI * 2);
    ctx.fillStyle = 'rgba(0, 0, 0, 0.55)';
    ctx.fill();

    // 2. Hitstun Flashing
    if (this.state === FighterState.HITSTUN && Math.floor(Date.now() / 40) % 2 === 0) {
      ctx.fillStyle = '#ffffff';
      ctx.fillRect(hurtbox.x, hurtbox.y, hurtbox.w, hurtbox.h);
      ctx.restore();
      return;
    }

    // 3. Knockout Rotation Collapse
    if (this.state === FighterState.KNOCKED_OUT) {
      ctx.translate(centerX, ARENA.GROUND_Y - 15);
      ctx.rotate(facing * Math.PI / 2.3);
      ctx.translate(-centerX, -(ARENA.GROUND_Y - 15));
    }

    // Breathing / Walk Animation Offsets
    const breathY = (this.grounded && this.state === FighterState.IDLE) ? Math.sin(Date.now() / 200) * 2.5 : 0;
    const walkSwing = (this.state === FighterState.WALKING) ? Math.sin(Date.now() / 100) * 12 : 0;
    const headY = hurtbox.y + 12 + breathY;
    const torsoY = hurtbox.y + 26 + breathY;
    const torsoH = this.height * 0.42;
    const legsY = torsoY + torsoH;

    // --- LEGS & BOOTS ---
    ctx.lineWidth = 6;
    ctx.lineCap = 'round';
    // Back leg
    ctx.strokeStyle = '#0f172a';
    ctx.beginPath();
    ctx.moveTo(centerX - (facing * 8), legsY);
    ctx.lineTo(centerX - (facing * 8) - walkSwing, ARENA.GROUND_Y - 4);
    ctx.stroke();

    // Front leg
    ctx.strokeStyle = this.color;
    ctx.beginPath();
    ctx.moveTo(centerX + (facing * 8), legsY);
    ctx.lineTo(centerX + (facing * 8) + walkSwing, ARENA.GROUND_Y - 4);
    ctx.stroke();

    // Armored Boots
    ctx.fillStyle = '#1e293b';
    ctx.fillRect(centerX + (facing * 8) + walkSwing - (facing > 0 ? 4 : 12), ARENA.GROUND_Y - 10, 16, 8);
    ctx.fillRect(centerX - (facing * 8) - walkSwing - (facing > 0 ? 4 : 12), ARENA.GROUND_Y - 10, 16, 8);

    // --- ARMORED TORSO & CHEST PLATE ---
    ctx.shadowBlur = (this.state === FighterState.SPECIAL) ? 22 : 12;
    ctx.shadowColor = this.color;

    // Main Torso Armor with Gradient
    const torsoGrad = ctx.createLinearGradient(hurtbox.x, torsoY, hurtbox.x + hurtbox.w, torsoY + torsoH);
    torsoGrad.addColorStop(0, this.color);
    torsoGrad.addColorStop(1, this.accentColor);
    ctx.fillStyle = torsoGrad;

    ctx.beginPath();
    if (ctx.roundRect) {
      ctx.roundRect(hurtbox.x + 4, torsoY, hurtbox.w - 8, torsoH, 8);
    } else {
      ctx.rect(hurtbox.x + 4, torsoY, hurtbox.w - 8, torsoH);
    }
    ctx.fill();

    // Chest Plate Bevel Detail
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.35)';
    ctx.lineWidth = 1.5;
    ctx.stroke();

    // Cyber Energy Core in Center
    ctx.beginPath();
    ctx.arc(centerX + (facing * 4), torsoY + torsoH * 0.42, 6, 0, Math.PI * 2);
    ctx.fillStyle = isP1 ? '#00e5ff' : '#ff2a6d';
    ctx.shadowBlur = 15;
    ctx.shadowColor = ctx.fillStyle;
    ctx.fill();

    // Shoulder Pauldrons
    ctx.fillStyle = '#0f172a';
    ctx.fillRect(centerX - (facing > 0 ? 18 : 6), torsoY + 2, 12, 12);
    ctx.strokeStyle = this.color;
    ctx.lineWidth = 2;
    ctx.strokeRect(centerX - (facing > 0 ? 18 : 6), torsoY + 2, 12, 12);

    // --- HEAD & VISOR ---
    ctx.beginPath();
    ctx.arc(centerX, headY, 15, 0, Math.PI * 2);
    ctx.fillStyle = '#0b1329';
    ctx.fill();
    ctx.lineWidth = 2.5;
    ctx.strokeStyle = this.color;
    ctx.stroke();

    // Glowing Cybernetic Visor Eye
    ctx.beginPath();
    const visorX = centerX + (facing > 0 ? 2 : -11);
    ctx.rect(visorX, headY - 4, 10, 6);
    ctx.fillStyle = isP1 ? '#00e5ff' : '#ff2a6d';
    ctx.shadowBlur = 14;
    ctx.shadowColor = ctx.fillStyle;
    ctx.fill();

    // Cyber Antenna / Crest
    ctx.beginPath();
    ctx.moveTo(centerX, headY - 14);
    ctx.lineTo(centerX + (facing * 6), headY - 24);
    ctx.strokeStyle = isP1 ? '#00e5ff' : '#ff2a6d';
    ctx.lineWidth = 2;
    ctx.stroke();

    // --- ARMS & COMBAT POSES ---
    if (this.state === FighterState.LIGHT_ATTACK || this.state === FighterState.HEAVY_ATTACK) {
      // Thrusting Arm
      ctx.lineWidth = 7;
      ctx.strokeStyle = this.color;
      ctx.beginPath();
      ctx.moveTo(centerX, torsoY + 12);
      const reach = (this.state === FighterState.HEAVY_ATTACK) ? 46 : 32;
      ctx.lineTo(centerX + (facing * reach), torsoY + 14);
      ctx.stroke();

      // Glowing Fist
      ctx.beginPath();
      ctx.arc(centerX + (facing * reach), torsoY + 14, 8, 0, Math.PI * 2);
      ctx.fillStyle = '#ffffff';
      ctx.shadowBlur = 18;
      ctx.shadowColor = this.color;
      ctx.fill();
    } else if (this.state === FighterState.BLOCKING) {
      // Guarding Arms
      ctx.lineWidth = 6;
      ctx.strokeStyle = this.color;
      ctx.beginPath();
      ctx.moveTo(centerX - (facing * 4), torsoY + 10);
      ctx.lineTo(centerX + (facing * 16), torsoY + 6);
      ctx.lineTo(centerX + (facing * 16), torsoY + 24);
      ctx.stroke();

      // Hexagonal Holographic Energy Barrier
      ctx.save();
      const barrierX = centerX + (facing * 34);
      const barrierY = torsoY + 20;
      ctx.strokeStyle = '#00ff88';
      ctx.lineWidth = 3;
      ctx.shadowBlur = 18;
      ctx.shadowColor = '#00ff88';
      ctx.beginPath();
      for (let i = 0; i < 6; i++) {
        const a = (i * Math.PI / 3);
        const bx = barrierX + Math.cos(a) * 32;
        const by = barrierY + Math.sin(a) * 36;
        if (i === 0) ctx.moveTo(bx, by);
        else ctx.lineTo(bx, by);
      }
      ctx.closePath();
      ctx.fillStyle = 'rgba(0, 255, 136, 0.22)';
      ctx.fill();
      ctx.stroke();
      ctx.restore();
    } else {
      // Idle / Guard Arm
      ctx.lineWidth = 5;
      ctx.strokeStyle = '#1e293b';
      ctx.beginPath();
      ctx.moveTo(centerX, torsoY + 10);
      ctx.lineTo(centerX + (facing * 14), torsoY + 20);
      ctx.stroke();
    }

    // --- ATTACK SHOCKWAVE CRESCENT ---
    if (this.currentHitbox) {
      const hb = this.currentHitbox;
      ctx.save();
      ctx.fillStyle = (hb.type === 'SPECIAL') ? 'rgba(255, 42, 109, 0.45)' : 'rgba(0, 229, 255, 0.35)';
      ctx.strokeStyle = (hb.type === 'SPECIAL') ? '#ff2a6d' : '#00e5ff';
      ctx.lineWidth = 3.5;
      ctx.shadowBlur = 15;
      ctx.shadowColor = ctx.strokeStyle;
      ctx.beginPath();
      ctx.arc(centerX + (facing * 42), torsoY + 16, (hb.type === 'SPECIAL' ? 56 : 38), -0.65, 0.65, facing < 0);
      ctx.stroke();
      ctx.restore();
    }

    // --- PLAYER INDICATOR PILL ---
    ctx.font = 'bold 12px "Segoe UI", sans-serif';
    ctx.textAlign = 'center';
    ctx.fillStyle = isP1 ? '#00e5ff' : '#ff2a6d';
    ctx.shadowBlur = 8;
    ctx.shadowColor = ctx.fillStyle;
    ctx.fillText(`P${this.playerIndex} • ${this.name}`, centerX, hurtbox.y - 12);

    ctx.restore();
  }
}
