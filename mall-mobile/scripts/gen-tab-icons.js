/**
 * Generate minimal valid PNG tab icons using Node.js built-ins.
 * Creates simple geometric icons for: home, category, order, mine (normal + active).
 */
const fs = require('fs')
const path = require('path')
const zlib = require('zlib')

const SIZE = 81 // 81x81 px (3x for 27dp)
const OUT = path.join(__dirname, '..', 'src', 'static', 'tab')

// RGBA pixel buffer
function createBuffer(size) {
  return Buffer.alloc(size * size * 4, 0)
}

function setPixel(buf, size, x, y, r, g, b, a = 255) {
  if (x < 0 || x >= size || y < 0 || y >= size) return
  const i = (y * size + x) * 4
  buf[i] = r; buf[i+1] = g; buf[i+2] = b; buf[i+3] = a
}

function fillCircle(buf, size, cx, cy, radius, r, g, b, a = 255) {
  for (let y = cy - radius; y <= cy + radius; y++) {
    for (let x = cx - radius; x <= cx + radius; x++) {
      if ((x - cx) ** 2 + (y - cy) ** 2 <= radius ** 2) {
        setPixel(buf, size, Math.round(x), Math.round(y), r, g, b, a)
      }
    }
  }
}

function fillRect(buf, size, x1, y1, x2, y2, r, g, b, a = 255) {
  for (let y = y1; y <= y2; y++) {
    for (let x = x1; x <= x2; x++) {
      setPixel(buf, size, x, y, r, g, b, a)
    }
  }
}

function drawLine(buf, size, x1, y1, x2, y2, thickness, r, g, b, a = 255) {
  const dx = x2 - x1, dy = y2 - y1
  const len = Math.sqrt(dx * dx + dy * dy)
  const steps = Math.ceil(len) * 2
  for (let i = 0; i <= steps; i++) {
    const t = i / steps
    const cx = x1 + dx * t, cy = y1 + dy * t
    fillCircle(buf, size, Math.round(cx), Math.round(cy), thickness, r, g, b, a)
  }
}

function encodePNG(buf, size) {
  // PNG signature
  const sig = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10])

  // IHDR
  const ihdr = Buffer.alloc(13)
  ihdr.writeUInt32BE(size, 0)  // width
  ihdr.writeUInt32BE(size, 4)  // height
  ihdr[8] = 8   // bit depth
  ihdr[9] = 6   // color type RGBA
  ihdr[10] = 0  // compression
  ihdr[11] = 0  // filter
  ihdr[12] = 0  // interlace

  // IDAT - raw pixel data with filter byte per row
  const raw = Buffer.alloc(size * (1 + size * 4))
  for (let y = 0; y < size; y++) {
    raw[y * (1 + size * 4)] = 0 // filter: none
    buf.copy(raw, y * (1 + size * 4) + 1, y * size * 4, (y + 1) * size * 4)
  }
  const compressed = zlib.deflateSync(raw)

  // IEND
  const iend = Buffer.alloc(0)

  function chunk(type, data) {
    const len = Buffer.alloc(4)
    len.writeUInt32BE(data.length, 0)
    const typeB = Buffer.from(type, 'ascii')
    const crcData = Buffer.concat([typeB, data])
    const crc = Buffer.alloc(4)
    crc.writeUInt32BE(crc32(crcData) >>> 0, 0)
    return Buffer.concat([len, typeB, data, crc])
  }

  return Buffer.concat([
    sig,
    chunk('IHDR', ihdr),
    chunk('IDAT', compressed),
    chunk('IEND', iend),
  ])
}

// CRC32
function crc32(buf) {
  let table = crc32.table
  if (!table) {
    table = crc32.table = new Uint32Array(256)
    for (let i = 0; i < 256; i++) {
      let c = i
      for (let j = 0; j < 8; j++) c = (c & 1) ? (0xEDB88320 ^ (c >>> 1)) : (c >>> 1)
      table[i] = c
    }
  }
  let crc = 0xFFFFFFFF
  for (let i = 0; i < buf.length; i++) {
    crc = table[(crc ^ buf[i]) & 0xFF] ^ (crc >>> 8)
  }
  return (crc ^ 0xFFFFFFFF) >>> 0
}

// Icon drawing functions
function drawHome(buf, size, r, g, b) {
  const s = size
  const cx = s / 2, base = s * 0.78, top = s * 0.22
  // Roof triangle
  for (let y = top; y <= s * 0.5; y++) {
    const progress = (y - top) / (s * 0.5 - top)
    const halfW = progress * s * 0.4
    for (let x = cx - halfW; x <= cx + halfW; x++) {
      setPixel(buf, size, Math.round(x), Math.round(y), r, g, b)
    }
  }
  // Body rectangle
  fillRect(buf, size, Math.round(s * 0.25), Math.round(s * 0.5), Math.round(s * 0.75), Math.round(base), r, g, b)
  // Door cutout (transparent)
  fillRect(buf, size, Math.round(s * 0.4), Math.round(s * 0.55), Math.round(s * 0.6), Math.round(base), 0, 0, 0, 0)
}

function drawCategory(buf, size, r, g, b) {
  const s = size
  const gap = 4, cellW = Math.floor((s - gap * 3) / 2), cellH = Math.floor((s - gap * 3) / 2)
  const positions = [
    [gap, gap],
    [gap * 2 + cellW, gap],
    [gap, gap * 2 + cellH],
    [gap * 2 + cellW, gap * 2 + cellH],
  ]
  for (const [px, py] of positions) {
    fillRect(buf, size, px, py, px + cellW, py + cellH, r, g, b)
  }
}

function drawOrder(buf, size, r, g, b) {
  const s = size
  const left = Math.round(s * 0.2), right = Math.round(s * 0.8)
  const top = Math.round(s * 0.15), bottom = Math.round(s * 0.85)
  const lineW = 3
  // Outer rect
  for (let y = top; y <= bottom; y++) {
    for (let x = left; x <= right; x++) {
      if (x < left + lineW || x > right - lineW || y < top + lineW || y > bottom - lineW) {
        setPixel(buf, size, x, y, r, g, b)
      }
    }
  }
  // Lines inside
  const lines = [0.35, 0.5, 0.65]
  for (const ly of lines) {
    fillRect(buf, size, Math.round(s * 0.3), Math.round(s * ly), Math.round(s * 0.7), Math.round(s * ly) + lineW, r, g, b)
  }
}

function drawMine(buf, size, r, g, b) {
  const s = size
  const cx = s / 2
  // Head circle
  fillCircle(buf, size, Math.round(cx), Math.round(s * 0.28), Math.round(s * 0.15), r, g, b)
  // Body arc (approximated as half-ellipse)
  for (let y = s * 0.52; y <= s * 0.8; y++) {
    const progress = (y - s * 0.52) / (s * 0.8 - s * 0.52)
    const halfW = (0.3 + 0.1 * Math.sin(progress * Math.PI)) * s
    for (let x = cx - halfW; x <= cx + halfW; x++) {
      if ((x - cx) ** 2 / halfW ** 2 + ((y - s * 0.52) / (s * 0.28)) ** 2 <= 1) {
        setPixel(buf, size, Math.round(x), Math.round(y), r, g, b)
      }
    }
  }
}

// Generate all icons
const NORMAL = { r: 107, g: 114, b: 128 }   // #6B7280
const ACTIVE = { r: 37, g: 99, b: 235 }      // #2563EB

const icons = [
  { name: 'home', draw: drawHome },
  { name: 'category', draw: drawCategory },
  { name: 'order', draw: drawOrder },
  { name: 'mine', draw: drawMine },
]

for (const icon of icons) {
  for (const [suffix, color] of [['', NORMAL], ['-active', ACTIVE]]) {
    const buf = createBuffer(SIZE)
    icon.draw(buf, SIZE, color.r, color.g, color.b)
    const png = encodePNG(buf, SIZE)
    const outPath = path.join(OUT, `${icon.name}${suffix}.png`)
    fs.writeFileSync(outPath, png)
    console.log(`Generated: ${outPath} (${png.length} bytes)`)
  }
}

// Also generate cart icons (not used in tabbar but exist)
for (const [suffix, color] of [['', NORMAL], ['-active', ACTIVE]]) {
  const buf = createBuffer(SIZE)
  // Simple cart icon
  const s = SIZE
  const cx = s / 2
  // Cart body
  fillRect(buf, s, Math.round(s*0.2), Math.round(s*0.3), Math.round(s*0.8), Math.round(s*0.35), color.r, color.g, color.b)
  // Cart handle
  drawLine(buf, s, Math.round(s*0.15), Math.round(s*0.3), Math.round(s*0.25), Math.round(s*0.15), 3, color.r, color.g, color.b)
  // Wheels
  fillCircle(buf, s, Math.round(s*0.35), Math.round(s*0.55), Math.round(s*0.06), color.r, color.g, color.b)
  fillCircle(buf, s, Math.round(s*0.65), Math.round(s*0.55), Math.round(s*0.06), color.r, color.g, color.b)
  const png = encodePNG(buf, SIZE)
  const outPath = path.join(OUT, `cart${suffix}.png`)
  fs.writeFileSync(outPath, png)
  console.log(`Generated: ${outPath} (${png.length} bytes)`)
}

console.log('All tab icons generated!')
