# DSA Election Night 2025 Results Tracker

A single-page web application to track real-time election results for DSA nationally endorsed candidates.

## Tech Stack

- **ClojureScript** - Application language
- **Reagent** - React wrapper for ClojureScript
- **Re-frame** - State management
- **Shadow-CLJS** - Build tool and dev server
- **Bulma CSS** - UI framework
- **Airtable** - Real-time data backend
- **R2** - caching
- **Github Actions** - Deployment, caching updates

## Setup

### Prerequisites

- Node.js (v16 or higher)
- Java (for ClojureScript compiler)

### Installation

```bash
npm install
```

### Airtable Configuration

1. Follow the instructions in `AIRTABLE_SCHEMA.md` to set up your Airtable base
2. Create a `.env` file in the project root:

```
AIRTABLE_API_KEY=your_api_key_here
AIRTABLE_BASE_ID=your_base_id_here
AIRTABLE_TABLE_NAME=Races
```
### Deployment
Builds to Github Pages via Github Actions. Tokens scope-limited to custom domains/read access. DNS records and CNAME set in a typical GH pages flow.

### Data Caching with Cloudflare R2

Airtable limits us to 5 requests/second per base so we can't use it publicly on election night. To resolve this, we poll the Airtable and cache the results in `data.json` and store it on R2. Users load R2's data.json first and only fallback to Airtable's API if it fails. This is a quick cache hack because I didn't want to ssh into a server and setup an actual caching proxy or DB.

#### How It Works

1. **Data Flow:**
   - GitHub Actions fetches latest data from Airtable every 5 minutes (free tier means scheduled runs could take 15-30 minutes)
   - Data is uploaded to Cloudflare R2 bucket as `data.json`
   - The app fetches from R2 (or local `/data.json` in development)
   - App auto-refreshes data every 30 seconds

2. **Local Development:**
   ```bash
   # Fetch latest data from Airtable to local cache
   npm run fetch-airtable
   # Start dev server (uses local /data.json)
   npm start
   ```

3. **Production Setup:**

   The app is configured to fetch from R2 in production via the `R2_DATA_URL` environment variable:

   - **R2 Bucket:** `2025-election`
   - **Public URL:** `https://pub-83c6936810744c479bd0abe7c3146c24.r2.dev/data.json` (developer link, rate-limited and should use a custom domain to remove rate-limit)
   - **Update Frequency:** Every 5 minutes via GitHub Actions  (free tier means scheduled runs could take 15-30 minutes)

4. **GitHub Secrets Required:**

   Add these secrets to your repository (Settings → Secrets and variables → Actions):

   - `R2_ACCESS_KEY_ID` - R2 API access key
   - `R2_SECRET_ACCESS_KEY` - R2 API secret key
   - `R2_ENDPOINT_URL` - R2 endpoint URL
   - `R2_BUCKET_NAME` - R2 bucket name
   - `AIRTABLE_API_KEY` - Airtable API key
   - `AIRTABLE_BASE_ID` - Airtable base ID

### Development

Start the development server:

```bash
npm start
```

The app will be available at `http://localhost:8080`

Hot reload is enabled - changes to ClojureScript files will update automatically.

### Production Build

Build the optimized production bundle:

```bash
npm run build
```

The compiled output will be in `public/js/main.js`
