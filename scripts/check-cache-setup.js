#!/usr/bin/env node

/**
 * Diagnostic script to verify Airtable caching setup
 * Run this to check if everything is configured correctly
 */

const fs = require('fs');
const path = require('path');

console.log('\n🔍 Airtable Cache Setup Diagnostic\n');
console.log('='.repeat(50));

let allGood = true;

// Check 1: Environment variables
console.log('\n1️⃣  Checking environment variables...');
require('dotenv').config();

const apiKey = process.env.AIRTABLE_API_KEY;
const baseId = process.env.AIRTABLE_BASE_ID;
const tableName = process.env.AIRTABLE_TABLE_NAME || 'Races';

if (!apiKey || !baseId) {
  console.log('   ❌ Missing environment variables');
  if (!apiKey) console.log('      - AIRTABLE_API_KEY is not set');
  if (!baseId) console.log('      - AIRTABLE_BASE_ID is not set');
  console.log('   💡 Create a .env file with these variables');
  allGood = false;
} else {
  console.log('   ✅ Environment variables are set');
  console.log(`      - AIRTABLE_API_KEY: ${apiKey.substring(0, 8)}...`);
  console.log(`      - AIRTABLE_BASE_ID: ${baseId}`);
  console.log(`      - AIRTABLE_TABLE_NAME: ${tableName}`);
}

// Check 2: Cache file exists
console.log('\n2️⃣  Checking cache file...');
const cacheFile = path.join(__dirname, '..', 'public', 'data.json');

if (!fs.existsSync(cacheFile)) {
  console.log('   ⚠️  Cache file does not exist yet');
  console.log('   💡 Run: npm run fetch-airtable');
  allGood = false;
} else {
  const stats = fs.statSync(cacheFile);
  const data = JSON.parse(fs.readFileSync(cacheFile, 'utf8'));

  console.log('   ✅ Cache file exists');
  console.log(`      - Path: ${cacheFile}`);
  console.log(`      - Size: ${(stats.size / 1024).toFixed(2)} KB`);
  console.log(`      - Records: ${data.records?.length || 0}`);
  console.log(`      - Last updated: ${data.cached_at || 'Unknown'}`);

  // Check if cache is stale (older than 15 minutes)
  if (data.cached_at) {
    const cacheAge = Date.now() - new Date(data.cached_at).getTime();
    const ageMinutes = Math.floor(cacheAge / 60000);

    if (ageMinutes > 15) {
      console.log(`   ⚠️  Cache is ${ageMinutes} minutes old (consider refreshing)`);
    } else {
      console.log(`   ✅ Cache is fresh (${ageMinutes} minutes old)`);
    }
  }
}

// Check 3: GitHub workflow files
console.log('\n3️⃣  Checking GitHub workflows...');
const workflowDir = path.join(__dirname, '..', '.github', 'workflows');

if (!fs.existsSync(workflowDir)) {
  console.log('   ❌ .github/workflows directory not found');
  allGood = false;
} else {
  const workflows = [
    'update-cache.yml',
    'update-cache-frequent.yml'
  ];

  workflows.forEach(workflow => {
    const workflowPath = path.join(workflowDir, workflow);
    if (fs.existsSync(workflowPath)) {
      console.log(`   ✅ ${workflow} exists`);
    } else {
      console.log(`   ❌ ${workflow} not found`);
      allGood = false;
    }
  });
}

// Check 4: Node scripts
console.log('\n4️⃣  Checking scripts...');
const scriptsToCheck = [
  'fetch-airtable.js'
];

scriptsToCheck.forEach(script => {
  const scriptPath = path.join(__dirname, script);
  if (fs.existsSync(scriptPath)) {
    console.log(`   ✅ scripts/${script} exists`);
  } else {
    console.log(`   ❌ scripts/${script} not found`);
    allGood = false;
  }
});

// Check 5: package.json scripts
console.log('\n5️⃣  Checking npm scripts...');
const packageJson = require(path.join(__dirname, '..', 'package.json'));

if (packageJson.scripts && packageJson.scripts['fetch-airtable']) {
  console.log('   ✅ npm script "fetch-airtable" is configured');
} else {
  console.log('   ❌ npm script "fetch-airtable" not found in package.json');
  allGood = false;
}

// Check 6: Application code
console.log('\n6️⃣  Checking application integration...');
const airtableCljs = path.join(__dirname, '..', 'src', 'app', 'airtable.cljs');

if (!fs.existsSync(airtableCljs)) {
  console.log('   ❌ src/app/airtable.cljs not found');
  allGood = false;
} else {
  const content = fs.readFileSync(airtableCljs, 'utf8');

  if (content.includes('fetch-races-from-cache')) {
    console.log('   ✅ Cache-first logic is implemented');
  } else {
    console.log('   ⚠️  Cache logic might not be implemented');
    console.log('      Check if fetch-races-from-cache function exists');
  }

  if (content.includes('/data.json')) {
    console.log('   ✅ Application fetches from /data.json');
  } else {
    console.log('   ⚠️  Application might not be fetching from cache');
  }
}

// Final summary
console.log('\n' + '='.repeat(50));
if (allGood) {
  console.log('✅ ALL CHECKS PASSED!\n');
  console.log('Your Airtable caching setup looks good.');
  console.log('\nNext steps:');
  console.log('1. Push your changes to GitHub');
  console.log('2. Add GitHub secrets (AIRTABLE_API_KEY, AIRTABLE_BASE_ID)');
  console.log('3. Check Actions tab to verify workflows run');
  console.log('4. Test your site to confirm cache is being used\n');
} else {
  console.log('❌ SOME CHECKS FAILED\n');
  console.log('Please address the issues above.');
  console.log('See CACHE_WORKFLOW_TROUBLESHOOTING.md for help.\n');
  process.exit(1);
}
