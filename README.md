# DSA Election Night 2025 Results Tracker

A single-page web application to track real-time election results for DSA nationally endorsed candidates.

## Tech Stack

- **ClojureScript** - Application language
- **Reagent** - React wrapper for ClojureScript
- **Re-frame** - State management
- **Shadow-CLJS** - Build tool and dev server
- **Bulma CSS** - UI framework
- **Airtable** - Real-time data backend

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
