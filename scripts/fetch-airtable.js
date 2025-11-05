#!/usr/bin/env node

/**
 * Fetch Airtable data and save to public/data.json
 * This script is used by GitHub Actions to cache Airtable data and avoid rate limits
 */

const https = require('https');
const fs = require('fs');
const path = require('path');

// Load environment variables
require('dotenv').config();

const API_KEY = process.env.AIRTABLE_API_KEY;
const BASE_ID = process.env.AIRTABLE_BASE_ID;
const TABLE_NAME = process.env.AIRTABLE_TABLE_NAME || 'Races';

if (!API_KEY || !BASE_ID) {
  console.error('Error: AIRTABLE_API_KEY and AIRTABLE_BASE_ID must be set');
  process.exit(1);
}

const url = `https://api.airtable.com/v0/${BASE_ID}/${encodeURIComponent(TABLE_NAME)}`;

const options = {
  headers: {
    'Authorization': `Bearer ${API_KEY}`
  }
};

console.log(`Fetching data from Airtable: ${TABLE_NAME}...`);

https.get(url, options, (res) => {
  let data = '';

  res.on('data', (chunk) => {
    data += chunk;
  });

  res.on('end', () => {
    if (res.statusCode !== 200) {
      console.error(`Error: Airtable API returned status ${res.statusCode}`);
      console.error(data);
      process.exit(1);
    }

    try {
      const response = JSON.parse(data);

      // Add metadata about when this cache was created
      const cacheData = {
        cached_at: new Date().toISOString(),
        records: response.records
      };

      const outputPath = path.join(__dirname, '..', 'public', 'data.json');

      // Ensure public directory exists
      const publicDir = path.dirname(outputPath);
      if (!fs.existsSync(publicDir)) {
        fs.mkdirSync(publicDir, { recursive: true });
      }

      fs.writeFileSync(outputPath, JSON.stringify(cacheData, null, 2));

      console.log(`Successfully cached ${response.records.length} records to ${outputPath}`);
      console.log(`Cache timestamp: ${cacheData.cached_at}`);
    } catch (error) {
      console.error('Error processing Airtable response:', error);
      process.exit(1);
    }
  });
}).on('error', (error) => {
  console.error('Error fetching from Airtable:', error);
  process.exit(1);
});
