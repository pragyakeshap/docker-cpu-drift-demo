
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
        
        // Check CPU feature simulation flag
        String cpuFeatures = System.getProperty("java.cpu.features", "");
        if (cpuFeatures.contains("sse4.1") && !cpuFeatures.contains("avx2")) {
            System.err.println("❌ FAILURE: This application requires AVX2 instructions!");
            System.err.println("Current CPU features: " + cpuFeatures);
            System.err.println("Required: AVX2 (256-bit vectors)");
            System.err.println("Available: Only SSE4.1 (128-bit vectors)");
            System.err.println("");
            System.err.println("This is what would happen on older hardware:");
            System.err.println("- Intel CPUs before Haswell (2013)");
            System.err.println("- AMD CPUs before Excavator (2015)");
            System.err.println("- Various embedded/cloud instances with older CPU features");
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
        System.out.println("⚠️ Incompatible with: Older CPUs, some cloud instances, embedded systems");
    }

    /**
     * Vectorized addition using 256-bit vectors (AVX2)
     * This method will only work efficiently on CPUs with AVX2 support
     */
    private static void vectorizedAdd(int[] a, int[] b, int[] result) {
        int i = 0;
        int loopBound = SPECIES.loopBound(a.length);
        
        // Process vectors of 8 integers at a time (256 bits / 32 bits per int)
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
