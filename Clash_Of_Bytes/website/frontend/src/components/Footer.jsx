import React from 'react';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container">
        <div>
          <strong>CLASH OF BYTES</strong> &copy; 2026–2027. All Rights Reserved.
        </div>
        <div style={{ marginTop: '4px', color: '#94a3b8', fontSize: '0.85rem' }}>
          Integrated Engineering Project — MIT Academy of Engineering (MIT AOE), Pune
        </div>
        <div className="team-credits">
          Developed by: Atharva Gajendra Gaikwad &bull; Rohit Tathod &bull; Varsharani Shivaji Patil &bull; Sakshi Vasudev Pathare
        </div>
      </div>
    </footer>
  );
}
