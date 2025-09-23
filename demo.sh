#!/bin/bash

# Function to cleanup Docker on exit
cleanup() {
    echo
    echo "=============================================="
    echo "Shutting down Docker..."
    echo "=============================================="
    # Stop Docker Desktop on macOS
    osascript -e 'quit app "Docker Desktop"' 2>/dev/null || true
    echo "Docker shutdown initiated."
}

# Set trap to cleanup on script exit
trap cleanup EXIT

echo "=============================================="
echo "     CPU Drift Demonstration Script"
echo "=============================================="
echo

echo "Starting Docker Desktop..."
echo "=============================================="
# Start Docker Desktop on macOS
open -a "Docker Desktop"
echo "Waiting for Docker to start..."

# Wait for Docker daemon to be ready
while ! docker info >/dev/null 2>&1; do
    echo "⏳ Docker is starting up..."
    sleep 3
done
echo "✅ Docker is ready!"
echo

echo "This demo shows how the same container image can:"
echo "1. ✅ Run successfully on modern CPUs with AVX2"
echo "2. ❌ Fail or perform poorly on older CPUs"
# echo "3. ✅ Run everywhere with baseline settings"
echo

echo
echo "=============================================="
echo "Step 1: Building application..."
echo "=============================================="
mvn -q clean package
if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi
#read -p "Press Enter to continue to Docker image build..."

echo
echo "=============================================="
echo "Step 2: Building Docker image..."
echo "=============================================="
docker build -t cpu-drift:avx2 -f Dockerfile.avx2 . -q
docker build -t cpu-drift:no-avx2 -f Dockerfile.no-avx2 . -q
read -p "Press Enter to test AVX2 optimized version..."

echo
echo "=============================================="
echo "Step 3: Testing AVX2 Optimized Version"
echo "=============================================="
echo "📦 Deploying to MODERN hardware (Intel Haswell+ or AMD Excavator+)"
echo "Expected: Success with high-performance 256-bit vector operations"
echo

if docker run --rm cpu-drift:avx2; then
    echo
    echo "✅ SUCCESS: Application ran successfully on AVX2-capable hardware"
    echo "   → Vector operations performed at full 256-bit width"
else
    echo
    echo "❌ Unexpected failure on AVX2-capable hardware"
    echo "   → This indicates a build or configuration issue"
fi
read -p "Press Enter to try the same application on older CPU (No AVX2)..."

echo
echo "=============================================="
echo "Step 4: Deploying to LEGACY Hardware"
echo "=============================================="
echo "📦 Deploying same image to OLDER hardware (pre-2013 Intel, pre-2015 AMD)"
echo "Simulates: AWS t2.micro, older bare metal, legacy cloud instances"
# echo "Expected: CRASH with UnsupportedOperationException"
echo

# echo "💥 Watch for the REAL production failure scenario:"
if docker run --rm cpu-drift:no-avx2; then
    echo
    echo "⚠️  UNEXPECTED: Should have failed on non-AVX2 hardware"
else
    echo
    echo "❌ CONFIRMED: CPU drift failure demonstrated"
    echo "   → This is exactly what happens in production!"
    echo "   → Same Docker image, different CPU = runtime failure"
    echo "   → Service becomes unavailable on older hardware"
fi
# read -p "Press Enter to test baseline version..."

echo
# echo "=============================================="
# echo "Step 5: Testing Baseline Version"
# echo "=============================================="
# echo "Running with conservative settings..."
# echo

# docker run --rm cpu-drift:baseline
read -p "Press Enter to show summary..."

echo "=============================================="
echo "🎯 CPU DRIFT DEMONSTRATION COMPLETE"
echo "=============================================="
echo
echo "WHAT WE JUST DEMONSTRATED:"
echo "✅ Modern Hardware: AVX2 app runs successfully (256-bit vectors)"
echo "❌ Legacy Hardware: Same app crashes with UnsupportedOperationException"
echo
echo "REAL-WORLD IMPLICATIONS:"
echo "• Same Docker image behaves differently on different CPUs"
echo "• Runtime failures occur when CPU assumptions are violated"
echo "• Production deployments fail silently on incompatible hardware"
echo "• Service availability depends on underlying CPU architecture"
echo
echo "COMMON FAILURE SCENARIOS IN PRODUCTION:"
echo "• Kubernetes nodes with mixed CPU generations"
echo "• Cloud auto-scaling to cheaper/older instance types"
echo "• Docker containers moved between different data centers"
echo "• Microservice deployments across heterogeneous infrastructure"
echo
echo "🔑 KEY LESSON: Always test CPU compatibility in your deployment pipeline!"
echo "=============================================="
echo
echo "🎉 Demo completed successfully!"
echo "Docker will be shut down automatically..."
