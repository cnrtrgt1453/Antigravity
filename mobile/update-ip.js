const fs = require('fs');
const os = require('os');
const path = require('path');

function getLocalIp() {
  const interfaces = os.networkInterfaces();
  for (const name of Object.keys(interfaces)) {
    for (const iface of interfaces[name]) {
      if (iface.family === 'IPv4' && !iface.internal) {
        return iface.address;
      }
    }
  }
  return 'localhost';
}

const currentIp = getLocalIp();
const configPath = path.join(__dirname, 'src', 'config', 'index.ts');

let content = fs.readFileSync(configPath, 'utf8');

// Regex to find and replace IP addresses in URLs
const pythonRegex = /PYTHON_API_URL: 'http:\/\/[\d.]+:(8000)'/;
const javaRegex = /JAVA_API_URL: 'http:\/\/[\d.]+:(8080)'/;

content = content.replace(pythonRegex, `PYTHON_API_URL: 'http://${currentIp}:$1'`);
content = content.replace(javaRegex, `JAVA_API_URL: 'http://${currentIp}:$1'`);

fs.writeFileSync(configPath, content);
console.log(`✅ IP address updated to: ${currentIp}`);
