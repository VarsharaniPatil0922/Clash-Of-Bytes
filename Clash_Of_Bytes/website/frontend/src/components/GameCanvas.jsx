import React, { useEffect, useRef, useState } from 'react';
import { ClashGameEngine } from '../../../game-client/engine.js';
import { sounds } from '../../../game-client/audio.js';
import { api } from '../services/api.js';

export default function GameCanvas({ p1Char, p2Char, p1User, p2User, onMatchFinish }) {
  const canvasRef = useRef(null);
  const engineRef = useRef(null);
  const [matchResult, setMatchResult] = useState(null);
  const [isMuted, setIsMuted] = useState(false);
  const [saveStatus, setSaveStatus] = useState('');

  const p1Name = p1User?.username || 'Player 1';
  const p2Name = p2User?.username || 'Player 2';

  useEffect(() => {
    if (!canvasRef.current) return;

    const engine = new ClashGameEngine(
      canvasRef.current,
      { id: p1Char?.characterId || 4, name: p1Char?.characterName || 'Cyber Brawler' },
      { id: p2Char?.characterId || 3, name: p2Char?.characterName || 'Shadow Ninja' },
      async (result) => {
        setMatchResult(result);
        setSaveStatus('Saving match telemetry to database...');

        const payload = {
          player1UserId: p1User?.userId || 1,
          player2UserId: p2User?.userId || 2,
          player1Username: p1Name,
          player2Username: p2Name,
          player1Character: p1Char?.characterName || 'Cyber Brawler',
          player2Character: p2Char?.characterName || 'Shadow Ninja',
          player1FinalScore: result.p1Score,
          player2FinalScore: result.p2Score,
          winnerPlayerIndex: result.winnerPlayerIndex,
          winnerUserId: result.winnerPlayerIndex === 1 ? (p1User?.userId || 1) : (p2User?.userId || 2),
          winnerUsername: result.winnerPlayerIndex === 1 ? p1Name : p2Name,
          totalRounds: result.totalRounds,
          rounds: result.rounds,
        };

        try {
          await api.request('/matches', {
            method: 'POST',
            body: JSON.stringify(payload),
          });
          setSaveStatus('Match saved successfully to database!');
        } catch (err) {
          setSaveStatus(`Saved locally: ${err.message}`);
        }
      }
    );

    engineRef.current = engine;
    engine.start();

    return () => {
      engine.stop();
    };
  }, [p1Char, p2Char, p1User, p2User]);

  const handleRestart = () => {
    setMatchResult(null);
    setSaveStatus('');
    if (engineRef.current) {
      engineRef.current.stop();
      engineRef.current = new ClashGameEngine(
        canvasRef.current,
        { id: p1Char?.characterId || 4, name: p1Char?.characterName || 'Cyber Brawler' },
        { id: p2Char?.characterId || 3, name: p2Char?.characterName || 'Shadow Ninja' },
        engineRef.current.onMatchEnd
      );
      engineRef.current.start();
    }
  };

  const handleToggleMute = () => {
    const muted = sounds.toggleMute();
    setIsMuted(muted);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
      <div style={{ position: 'relative' }}>
        <div
          style={{
            border: '2px solid var(--neon-cyan)',
            boxShadow: '0 0 25px rgba(0, 229, 255, 0.4)',
            borderRadius: '8px',
            overflow: 'hidden',
            background: '#000',
          }}
        >
          <canvas ref={canvasRef} width={960} height={540} style={{ display: 'block', maxWidth: '100%' }} />
        </div>

        {/* Match Result Overlay Modal */}
        {matchResult && (
          <div
            style={{
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              background: 'rgba(6, 9, 19, 0.88)',
              backdropFilter: 'blur(8px)',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              borderRadius: '8px',
              padding: '24px',
            }}
          >
            <h2 style={{ fontSize: '2.5rem', color: 'var(--neon-gold)', textShadow: '0 0 20px rgba(255, 204, 0, 0.8)' }}>
              VICTORY!
            </h2>
            <div style={{ fontSize: '1.4rem', color: '#fff', margin: '10px 0' }}>
              {matchResult.winnerPlayerIndex === 1 ? p1Name : p2Name} Wins the Clash!
            </div>
            <div style={{ color: 'var(--neon-cyan)', fontSize: '1.1rem', marginBottom: '8px' }}>
              Final Score: {matchResult.p1Score} - {matchResult.p2Score}
            </div>
            <div style={{ color: '#00ff88', fontSize: '0.9rem', marginBottom: '20px' }}>
              {saveStatus}
            </div>

            <div style={{ display: 'flex', gap: '16px' }}>
              <button className="btn btn-primary" onClick={handleRestart}>
                Play Again
              </button>
              {onMatchFinish && (
                <button className="btn btn-secondary" onClick={onMatchFinish}>
                  Back to Select
                </button>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Control Buttons */}
      <div style={{ display: 'flex', gap: '14px', marginTop: '16px' }}>
        <button className="btn btn-primary btn-sm" onClick={handleRestart}>
          Restart Battle
        </button>
        <button className="btn btn-secondary btn-sm" onClick={handleToggleMute}>
          {isMuted ? 'Unmute Audio' : 'Mute Audio'}
        </button>
      </div>
    </div>
  );
}
