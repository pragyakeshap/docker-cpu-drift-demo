#!/bin/bash

echo "=============================================="
echo "     CPU Drift Demonstration Script"
echo "=============================================="
echo

echo "This demo shows how the same container image can:"
echo "1. ✅ Run successfully on modern CPUs with AVX2"
echo "2. ❌ Fail or perform poorly on older CPUs"
echo "3. ✅ Run everywhere with baseline settings"
echo

echo "Step 1: Building application..."
mvn -q clean package
if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "Step 2: Building Docker images..."
docker build -t cpu-drift:avx2 -f Dockerfile.avx2 . -q
docker build -t cpu-drift:baseline -f Dockerfile.baseline . -q
docker build -t cpu-drift:no-avx2 -f Dockerfile.no-avx2 . -q

echo
echo "=============================================="
echo "Step 3: Testing AVX2 Optimized Version"
echo "=============================================="
echo "Running with AVX2 optimizations..."
echo

docker run --rm cpu-drift:avx2

echo
echo "=============================================="
echo "Step 4: Simulating Older CPU (No AVX2)"
echo "=============================================="
echo "This simulates what happens on older hardware..."
echo

docker run --rm cpu-drift:no-avx2

echo
echo "=============================================="
echo "Step 5: Testing Baseline Version"
echo "=============================================="
echo "Running with conservative settings..."
echo

docker run --rm cpu-drift:baseline

echo
echo "=============================================="
echo "Summary:"
echo "=============================================="
echo "AVX2 Version    : Uses 256-bit vectors (faster on modern CPUs)"
echo "No-AVX2 Version : Limited to 128-bit vectors (older CPU simulation)"
echo "Baseline Version: Conservative settings (runs everywhere)"
echo
echo "CPU Drift Lesson: The same image can behave differently"
echo "depending on the underlying CPU architecture!"
echo "=============================================="
