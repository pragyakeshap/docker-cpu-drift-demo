#!/bin/bash

echo "🎯 True CPU Drift Demonstration"
echo "================================"
echo
echo "Step 1: Build SINGLE optimized image"
docker build -t cpu-drift:single -f Dockerfile.single-image .

echo
echo "Step 2: Test on your current CPU"
echo "This should work on modern CPUs (2013+):"
docker run --rm cpu-drift:single

echo
echo "Step 3: To see TRUE CPU drift, you would need to:"
echo "• Deploy this SAME image to an older cloud instance"
echo "• Try AWS t2.micro (older Xeon without AVX2)"
echo "• Try Google Cloud n1-standard-1 (pre-Haswell)"
echo "• Or use QEMU to emulate older CPU features"
echo
echo "The SAME image would:"
echo "✅ Work here (modern CPU)"
echo "❌ Crash there (older CPU)"
echo
echo "That's TRUE CPU drift - same image, different behavior!"
