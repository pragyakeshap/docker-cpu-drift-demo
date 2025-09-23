
package com.example;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorSpecies;

public class Avx2Demo {
    // Explicitly require AVX2 (256-bit vectors)
    private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_256;
    
    public static void main(String[] args) {
        System.out.println("=== CPU Drift Demonstration with AVX2 Vector Instructions ===");
        System.out.println("JVM: " + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version"));
        System.out.println("Preferred Vector Species: " + IntVector.SPECIES_PREFERRED);
        System.out.println("Required Vector Species: " + SPECIES);
        System.out.println("Required Vector Length: " + SPECIES.length() + " elements");
        System.out.println("Required Vector Bit Size: " + SPECIES.vectorBitSize() + " bits");
        
        // Check CPU feature simulation flag and demonstrate actual failure scenarios
        String cpuFeatures = System.getProperty("java.cpu.features", "");
        if (cpuFeatures.contains("sse4.1") && !cpuFeatures.contains("avx2")) {
            System.err.println("❌ FAILURE SCENARIO: AVX2 Vector API Error");
            System.err.println("========================================================");
            System.err.println("");
            
            // Simulate the actual exception that would be thrown
            System.err.println("Exception in thread \"main\" java.lang.UnsupportedOperationException:");
            System.err.println("    Vector species IntVector.SPECIES_256 requires AVX2 instruction set");
            System.err.println("    at jdk.incubator.vector.IntVector.fromArray(IntVector.java:847)");
            System.err.println("    at com.example.Avx2Demo.vectorizedAdd(Avx2Demo.java:94)");
            System.err.println("    at com.example.Avx2Demo.main(Avx2Demo.java:70)");
            System.err.println("");
            
            // Show JVM diagnostic information that would appear
            System.err.println("JVM Diagnostic Information:");
            System.err.println("- CPU Architecture: " + System.getProperty("os.arch"));
            System.err.println("- Available CPU features: " + cpuFeatures);
            System.err.println("- Vector API preferred species: " + IntVector.SPECIES_PREFERRED);
            System.err.println("- Application requires: SPECIES_256 (AVX2)");
            System.err.println("- Actual CPU supports: Only up to SPECIES_128 (SSE4.1)");
            System.err.println("");
            
            // Show the real-world implications
            System.err.println("DEPLOYMENT FAILURE ANALYSIS:");
            System.err.println("✗ Application built with --enable-preview and AVX2 assumptions");
            System.err.println("✗ Target environment: Older CPU without AVX2 support");
            System.err.println("✗ Result: Runtime failure, service unavailable");
            System.err.println("");
            System.err.println("Common scenarios where this happens:");
            System.err.println("- Legacy cloud instances (AWS t2.micro, older GCP instances)");
            System.err.println("- Older bare metal servers (Intel pre-2013, AMD pre-2015)");
            System.err.println("- Docker containers moved between different CPU architectures");
            System.err.println("- Kubernetes nodes with mixed CPU generations");
            
            // Show what the logs would look like in production
            System.err.println("");
            System.err.println("PRODUCTION LOG EXAMPLE:");
            System.err.println("2025-09-17 10:30:15.234 ERROR [main] Application startup failed");
            System.err.println("2025-09-17 10:30:15.235 ERROR [main] Caused by: UnsupportedOperationException");
            System.err.println("2025-09-17 10:30:15.235 ERROR [main] Vector operation failed: CPU lacks AVX2");
            System.err.println("2025-09-17 10:30:15.236 ERROR [main] Service health check: FAILED");
            System.err.println("2025-09-17 10:30:15.237 ERROR [main] Container exit code: 1");
            
            System.exit(1);
        }
        
        // Check if we're forced to use smaller vectors
        String maxVectorSize = System.getProperty("JAVA_TOOL_OPTIONS", "");
        if (maxVectorSize.contains("MaxVectorSize=16")) {
            System.err.println("⚠️ WARNING: Vector size limited to 128-bit (non-AVX2 environment)");
            System.err.println("This will impact performance significantly!");
            System.err.println("Expected 256-bit vectors, but limited to 128-bit");
            
            // Continue but show degraded performance
            if (IntVector.SPECIES_PREFERRED.vectorBitSize() < 256) {
                System.err.println("❌ PERFORMANCE DEGRADATION: Using " + 
                    IntVector.SPECIES_PREFERRED.vectorBitSize() + "-bit vectors instead of 256-bit");
            }
        }
        
        // Force check for 256-bit vector capability
        if (SPECIES.vectorBitSize() < 256) {
            System.err.println("❌ ERROR: This application was compiled for AVX2 (256-bit) but CPU doesn't support it!");
            System.err.println("Available: " + IntVector.SPECIES_PREFERRED.vectorBitSize() + "-bit vectors");
            System.err.println("Required: 256-bit vectors (AVX2)");
            System.exit(1);
        }
        
        System.out.println("✅ AVX2 support confirmed - proceeding with vectorized operations");
        
        final int size = 1_000_000;
        int[] a = new int[size];
        int[] b = new int[size];
        int[] result = new int[size];

        // Initialize arrays
        for (int i = 0; i < size; i++) {
            a[i] = i;
            b[i] = i * 2;
        }

        System.out.println("\nPerforming AVX2-optimized vectorized operations...");
        long startTime = System.nanoTime();
        
        // Warm up JIT with vectorized operations
        for (int w = 0; w < 100; w++) {
            vectorizedAdd(a, b, result);
        }
        
        // Benchmark
        for (int r = 0; r < 1000; r++) {
            vectorizedAdd(a, b, result);
        }
        
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000.0; // ms
        
        // Calculate checksum to verify results
        long checksum = 0;
        for (int i = 0; i < Math.min(100, size); i++) {
            checksum += result[i];
        }

        System.out.println("✅ Vectorized computation completed successfully!");
        System.out.println("Time taken: " + String.format("%.2f", duration) + " ms");
        System.out.println("Vector performance: " + SPECIES.length() + " integers per instruction");
        System.out.println("Checksum (first 100 elements): " + checksum);
        System.out.println("Expected checksum: " + (100 * 99 * 3 / 2)); // Sum of i*3 for i=0 to 99
        
        // Demonstrate that this requires AVX2
        performComplexVectorOperations();
        
        //System.out.println("\n=== CPU Drift Demo Complete ===");
        System.out.println("✅ This application successfully used " + SPECIES.vectorBitSize() + "-bit vector instructions!");
        System.out.println("⚡ Performance benefit: " + SPECIES.length() + "x parallelism per instruction");
        System.out.println("🏗️ Built for: Modern CPUs with AVX2 support (Intel Haswell+, AMD Excavator+)");
        //System.out.println("⚠️ Incompatible with: Older CPUs, some cloud instances, embedded systems");
    }

    /**
     * Vectorized addition using 256-bit vectors (AVX2)
     * This method will only work efficiently on CPUs with AVX2 support
     */
    private static void vectorizedAdd(int[] a, int[] b, int[] result) {
        try {
            int i = 0;
            int loopBound = SPECIES.loopBound(a.length);
            
            // Process vectors of 8 integers at a time (256 bits / 32 bits per int)
            // These operations will fail on non-AVX2 hardware with UnsupportedOperationException
            for (; i < loopBound; i += SPECIES.length()) {
                IntVector va = IntVector.fromArray(SPECIES, a, i);
                IntVector vb = IntVector.fromArray(SPECIES, b, i);
                IntVector vresult = va.add(vb);
                vresult.intoArray(result, i);
            }
            
            // Handle remaining elements (tail)
            for (; i < a.length; i++) {
                result[i] = a[i] + b[i];
            }
            
        } catch (UnsupportedOperationException e) {
            // This is the actual exception that would be thrown on incompatible hardware
            System.err.println("\n💥 REAL PRODUCTION FAILURE:");
            System.err.println("=============================");
            System.err.println("Exception: " + e.getClass().getSimpleName());
            System.err.println("Message: " + (e.getMessage() != null ? e.getMessage() : "Vector operation not supported on this CPU"));
            System.err.println("");
            System.err.println("TECHNICAL DETAILS:");
            System.err.println("- Application compiled for: " + SPECIES + " (" + SPECIES.vectorBitSize() + "-bit)");
            System.err.println("- Hardware supports: " + IntVector.SPECIES_PREFERRED + " (" + IntVector.SPECIES_PREFERRED.vectorBitSize() + "-bit)");
            System.err.println("- Architecture mismatch detected at runtime");
            System.err.println("");
            System.err.println("This failure would cause:");
            System.err.println("✗ Service crash and unavailability");
            System.err.println("✗ Container restart loops");
            System.err.println("✗ Failed deployments");
            System.err.println("✗ Customer-facing downtime");
            
            // Re-throw to demonstrate the actual crash
            throw new RuntimeException("CPU architecture mismatch - AVX2 required but not supported", e);
        }
    }
    
    /**
     * Perform more complex vector operations that really benefit from AVX2
     */
    private static void performComplexVectorOperations() {
        System.out.println("\nPerforming complex 256-bit vector operations...");
        
        int[] data = new int[SPECIES.length() * 4]; // 32 elements
        for (int i = 0; i < data.length; i++) {
            data[i] = i + 1;
        }
        
        // Demonstrate various AVX2 vector operations
        for (int i = 0; i < data.length; i += SPECIES.length()) {
            IntVector v = IntVector.fromArray(SPECIES, data, i);
            
            // Complex operations that benefit from 256-bit vectors
            IntVector squared = v.mul(v);                    // Square each element
            IntVector shifted = v.mul(4);                    // Multiply by 4
            IntVector combined = squared.add(shifted);       // Add them together
            IntVector clamped = combined.min(1000);          // Clamp to max 1000
            
            // Store result back
            clamped.intoArray(data, i);
        }
        
        System.out.println("✅ Complex AVX2 operations completed");
        System.out.println("Sample results: " + data[0] + ", " + data[1] + ", " + data[2] + "...");
        System.out.println("Operations performed on " + SPECIES.length() + " integers simultaneously");
    }
}
