// =======================================================================
// CLASH OF BYTES: WEB CLIENT SUPABASE CONFIGURATION (EXAMPLE TEMPLATE)
// Safe public configuration template for browser client.
//
// INSTRUCTIONS:
// 1. Copy this file to 'config.js' in the same folder:
//    copy config.example.js config.js
// 2. Set 'url' to your Supabase Project URL (e.g. https://xyzcompany.supabase.co)
// 3. Set 'publishableKey' to your Supabase Publishable / Anon Key (sbp_... or anon JWT)
//
// IMPORTANT SECURITY NOTICE:
// - ONLY use your public publishable/anon key here.
// - NEVER place your service-role secret key or PostgreSQL database password here!
// =======================================================================

export const SUPABASE_CONFIG = {
  // Supabase Project URL (from Supabase Dashboard -> Project Settings -> API)
  url: 'https://your-project-ref.supabase.co',

  // Supabase Public Publishable / Anon Key (Client-Safe)
  publishableKey: 'sbp_your_public_publishable_anon_key_here'
};
