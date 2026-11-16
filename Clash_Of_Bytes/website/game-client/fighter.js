// =======================================================================
// CLASH OF BYTES: 2D FIGHTER ENTITY CLASS
// Implements physics, collision hitboxes, state machine, and animation
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

    if (input.block && this.grounded) {
      this.state = FighterState.BLOCKING;
      this.vx = 0;
      return;
    } else if (this.state === FighterState.BLOCKING && !input.block) {
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

    if (input.lightAttack) {
      this.triggerLightAttack(soundSystem);
    } else if (input.heavyAttack) {
      this.triggerHeavyAttack(soundSystem);
    } else if (input.special) {
      this.triggerSpecial(soundSystem);
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
    const hurtbox = this.getHurtbox();
    const centerX = this.x;
    const isP1 = (this.playerIndex === 1);

    ctx.beginPath();
    ctx.ellipse(centerX, ARENA.GROUND_Y - 4, this.width * 0.7, 8, 0, 0, Math.PI * 2);
    ctx.fillStyle = 'rgba(0, 0, 0, 0.45)';
    ctx.fill();

    if (this.state === FighterState.HITSTUN && Math.floor(Date.now() / 40) % 2 === 0) {
      ctx.fillStyle = '#ffffff';
      ctx.fillRect(hurtbox.x, hurtbox.y, hurtbox.w, hurtbox.h);
      ctx.restore();
      return;
    }

    ctx.shadowBlur = (this.state === FighterState.SPECIAL) ? 20 : 10;
    ctx.shadowColor = this.color;

    const grad = ctx.createLinearGradient(hurtbox.x, hurtbox.y, hurtbox.x + hurtbox.w, hurtbox.y + hurtbox.h);
    grad.addColorStop(0, this.color);
    grad.addColorStop(1, this.accentColor);
    ctx.fillStyle = grad;

    ctx.beginPath();
    if (ctx.roundRect) {
      ctx.roundRect(hurtbox.x, hurtbox.y + 18, hurtbox.w, hurtbox.h - 18, 10);
    } else {
      ctx.rect(hurtbox.x, hurtbox.y + 18, hurtbox.w, hurtbox.h - 18);
    }
    ctx.fill();

    ctx.beginPath();
    ctx.arc(centerX, hurtbox.y + 12, 14, 0, Math.PI * 2);
    ctx.fillStyle = '#0f172a';
    ctx.fill();
    ctx.lineWidth = 2.5;
    ctx.strokeStyle = this.color;
    ctx.stroke();

    ctx.beginPath();
    const visorX = centerX + (this.facing > 0 ? 2 : -10);
    ctx.rect(visorX, hurtbox.y + 8, 8, 5);
    ctx.fillStyle = isP1 ? '#00e5ff' : '#ff2a6d';
    ctx.shadowBlur = 12;
    ctx.shadowColor = ctx.fillStyle;
    ctx.fill();

    if (this.state === FighterState.BLOCKING) {
      ctx.beginPath();
      const shieldX = centerX + (this.facing > 0 ? 25 : -25);
      ctx.arc(shieldX, hurtbox.y + 45, 35, -Math.PI / 2, Math.PI / 2, this.facing < 0);
      ctx.strokeStyle = '#ffcc00';
      ctx.lineWidth = 4;
      ctx.stroke();
    }

    if (this.currentHitbox) {
      const hb = this.currentHitbox;
      ctx.fillStyle = (hb.type === 'SPECIAL') ? 'rgba(255, 42, 109, 0.45)' : 'rgba(0, 229, 255, 0.35)';
      ctx.strokeStyle = (hb.type === 'SPECIAL') ? '#ff2a6d' : '#00e5ff';
      ctx.lineWidth = 3;
      ctx.beginPath();
      ctx.arc(centerX + (this.facing * 45), hurtbox.y + 40, (hb.type === 'SPECIAL' ? 55 : 35), -0.6, 0.6, this.facing < 0);
      ctx.stroke();
    }

    ctx.font = 'bold 12px sans-serif';
    ctx.textAlign = 'center';
    ctx.fillStyle = isP1 ? '#00e5ff' : '#ff2a6d';
    ctx.shadowBlur = 8;
    ctx.shadowColor = ctx.fillStyle;
    ctx.fillText(`P${this.playerIndex}`, centerX, hurtbox.y - 10);

    ctx.restore();
  }
}
