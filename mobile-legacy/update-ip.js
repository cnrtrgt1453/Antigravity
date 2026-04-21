const fs = require('fs');
const os = require('os');
const path = require('path');

function getLocalIp() {
  const interfaces = os.networkInterfaces();
  const addresses = [];
  
  for (const name of Object.keys(interfaces)) {
    for (const iface of interfaces[name]) {
      if (iface.family === 'IPv4' && !iface.internal) {
        // Skip common virtual network ranges (VirtualBox, VMware)
        if (iface.address.startsWith('192.168.56.') || iface.address.startsWith('192.168.17.') || iface.address.startsWith('192.168.200.') || iface.address.startsWith('169.254.')) {
          continue;
        }
        addresses.push(iface.address);
      }
    }
  }
  
  // Prioritize 192.168.x.x addresses as they are most common for WiFi/Local LAN
  const preferred = addresses.find(addr => addr.startsWith('192.168.'));
  if (preferred) return preferred;
  
  return addresses.length > 0 ? addresses[0] : 'localhost';
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
