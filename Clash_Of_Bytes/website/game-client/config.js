// =======================================================================
// CLASH OF BYTES: WEB CLIENT SUPABASE CONFIGURATION
// Public configuration for the browser frontend.
//
// NOTE: Only your public publishable/anon key belongs here.
// NEVER place your service-role secret key or PostgreSQL database password here!
// =======================================================================

export const SUPABASE_CONFIG = {
  // Supabase Project URL (e.g. https://your-project-id.supabase.co)
  url: (typeof window !== 'undefined' && window.__CLASH_CONFIG__?.supabaseUrl) || 'https://fzhtgfpdvefzjcqceqym.supabase.co',

  // Supabase Public Publishable / Anon Key (Client-Safe)
  publishableKey: (typeof window !== 'undefined' && window.__CLASH_CONFIG__?.supabasePublishableKey) || 'sb_publishable_bTuqcR6mt62hKhuR9mcHWQ_Fm7cdsWe'
};
