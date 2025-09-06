
# CPU Drift Demonstration with Java AVX2

This project demonstrates **CPU Drift** - the phenomenon where the same Docker container image behaves differently or fails when run on different CPU architectures.

## What is CPU Drift?

CPU Drift occurs when applications are optimized for specific CPU features (like AVX2 vector instructions) but are then deployed on hardware that doesn't support those features. This can lead to:

- **Runtime failures** on older CPUs
- **Performance degradation** when falling back to slower instruction sets
- **Inconsistent behavior** across different deployment environments

## The Demonstration

This demo uses Java's Vector API to create an application that leverages AVX2 (256-bit vector) instructions for high-performance computation.

### Three Scenarios:

1. **AVX2 Optimized** (`Dockerfile.avx2`) - Built for modern CPUs with AVX2 support
2. **No-AVX2 Simulation** (`Dockerfile.no-avx2`) - Simulates older hardware without AVX2
3. **Baseline** (`Dockerfile.baseline`) - Conservative settings for broad compatibility

## Quick Start

Run the complete demonstration:

```bash
./demo.sh
```

Or run individual containers:

```bash
# Build the application
mvn clean package

# Build Docker images
docker build -t cpu-drift:avx2 -f Dockerfile.avx2 .
docker build -t cpu-drift:baseline -f Dockerfile.baseline .
docker build -t cpu-drift:no-avx2 -f Dockerfile.no-avx2 .

# Test AVX2 version (optimized for modern CPUs)
docker run --rm cpu-drift:avx2

# Test no-AVX2 version (simulates older hardware - will fail)
docker run --rm cpu-drift:no-avx2

# Test baseline version (runs everywhere)
docker run --rm cpu-drift:baseline
```

## Expected Results

### 1. AVX2 Version (Modern CPU)
```
✅ AVX2 support confirmed - proceeding with vectorized operations
✅ This application successfully used 256-bit vector instructions!
⚡ Performance benefit: 8x parallelism per instruction
🏗️ Built for: Modern CPUs with AVX2 support (Intel Haswell+, AMD Excavator+)
```

### 2. No-AVX2 Version (Older CPU Simulation)
```
❌ SIMULATED FAILURE: This application requires AVX2 instructions!
Current CPU features: sse4.1
Required: AVX2 (256-bit vectors)
Available: Only SSE4.1 (128-bit vectors)

This is what would happen on older hardware:
- Intel CPUs before Haswell (2013)
- AMD CPUs before Excavator (2015)
- Various embedded/cloud instances with older CPU features
```

### 3. Baseline Version (Compatible)
```
✅ AVX2 support confirmed - proceeding with vectorized operations
✅ This application successfully used 256-bit vector instructions!
```

## Technical Details

### CPU Features Timeline

- **SSE4.1** (2006): 128-bit vectors, 4 integers per instruction
- **AVX** (2011): 256-bit vectors, but limited integer operations
- **AVX2** (2013): Full 256-bit integer vectors, 8 integers per instruction

### Hardware Compatibility

**✅ AVX2 Support:**
- Intel: Haswell (2013) and newer
- AMD: Excavator (2015) and newer
- Most modern cloud instances

**❌ No AVX2 Support:**
- Intel: Ivy Bridge (2012) and older
- AMD: Steamroller (2014) and older
- Some embedded systems
- Older cloud instances
- Virtualized environments with restricted CPU features
3. Re-run with a baseline JVM config (avoid AVX2) → **works everywhere**.

> Tip: If you don't have a non‑AVX2 host, spin a small cloud VM that lacks AVX2 (older Xeon) for the demo.

---

## Prereqs
- Java 17+ (or use Docker targets below)
- Docker (for containerized runs)
- VS Code with Java extensions (recommended)

---

## Project layout
- `src/main/java/com/example/Avx2Demo.java` — small hot loop to trigger JIT
- `pom.xml` — Maven build (Java 17)
- `Dockerfile.avx2` — runs HotSpot with `-XX:UseAVX=2` (forces AVX2)
- `Dockerfile.baseline` — runs HotSpot with `-XX:UseAVX=0` (no AVX)
- `.vscode/` — tasks & launch configs for VS Code
- `.devcontainer/` — optional Dev Container to hack in a clean env

---

## Local run (no Docker)
```bash
mvn -q clean package
# Force AVX2 to show "works here"
java -XX:+UnlockDiagnosticVMOptions -XX:UseAVX=2 -jar target/cpu-drift-demo-1.0.0.jar
# Baseline that avoids AVX — should run on old boxes too
java -XX:+UnlockDiagnosticVMOptions -XX:UseAVX=0 -jar target/cpu-drift-demo-1.0.0.jar
```

## Docker run (AVX2 forced) — use this on both hosts
```bash
# Build the AVX2 image
docker build -t cpu-drift:avx2 -f Dockerfile.avx2 .
# Run (likely OK on modern laptop)
docker run --rm cpu-drift:avx2

# Now try the same image on a host *without* AVX2 → expect "Illegal instruction"
```

## Docker run (baseline) — fallback that runs everywhere
```bash
docker build -t cpu-drift:baseline -f Dockerfile.baseline .
docker run --rm cpu-drift:baseline
```

### Expected outputs
- **AVX2 host**: Both images run; JIT reports using AVX >= 2.
- **Non‑AVX2 host**: `cpu-drift:avx2` may crash with `Illegal instruction`; `baseline` runs fine.

---

## Notes for the talk
- The JVM auto-detects CPU features and can JIT vectorized code using AVX2.
- When you force AVX2 (`UseAVX=2`) and move to a host without it, the process can crash at runtime.
- Pinning to a **baseline** (`UseAVX=0`) restores portability (at the cost of some performance).

---

## VS Code
- Press **⌘⇧B / Ctrl+Shift+B** to build via Maven task.
- Use **Run and Debug** → "Java: Run Avx2Demo (AVX2)" or "Java: Run Avx2Demo (Baseline)".
- Docker tasks provided for quick image builds.

Enjoy the demo!
