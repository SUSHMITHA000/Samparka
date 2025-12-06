package com.example.samparka;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.DataType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class ImageClassifier {

    private final Interpreter interpreter;
    private final List<String> labels;
    private final int imageSize = 224; // must match training input size
    private static final float THRESHOLD = 0.60f; // start with 0.60, tune using logs

    public ImageClassifier(Context context) throws IOException {
        MappedByteBuffer model = loadModelFile(context);
        interpreter = new Interpreter(model);
        labels = loadLabels(context);
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        try (AssetFileDescriptor fileDescriptor = context.getAssets().openFd("model.tflite");
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    private List<String> loadLabels(Context context) throws IOException {
        List<String> labelList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("labels.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                labelList.add(line);
            }
        }
        return labelList;
    }

    // Simple result holder: label + confidence
    public static class Result {
        public final String label;   // "none" means rejected
        public final float score;
        public Result(String label, float score) {
            this.label = label;
            this.score = score;
        }
    }

    public Result classify(Bitmap bitmap) {
        // 1) Resize to model input size
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true);

        // 2) Wrap in TensorImage (float32)
        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(resizedBitmap);

        // 3) Preprocess: resize + normalize 0..255 -> 0..1
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(imageSize, imageSize, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0f, 255f)) // divide by 255 to match Rescaling(1./255)
                .build();
        tensorImage = imageProcessor.process(tensorImage);

        // 4) Prepare output tensor [1, numClasses]
        int numClasses = labels.size();
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(
                new int[]{1, numClasses},
                DataType.FLOAT32
        );

        // 5) Run inference
        interpreter.run(tensorImage.getBuffer(), outputBuffer.getBuffer().rewind());

        // 6) Get probabilities and find max
        float[] probs = outputBuffer.getFloatArray();
        int maxIndex = 0;
        float maxProb = probs[0];
        for (int i = 1; i < probs.length; i++) {
            if (probs[i] > maxProb) {
                maxProb = probs[i];
                maxIndex = i;
            }
        }

        // Debug log: see what the model outputs
        Log.d("ML_DEBUG", "maxProb=" + maxProb + " label=" + labels.get(maxIndex));

        // 7) Apply rejection threshold
        if (maxProb < THRESHOLD) {
            // Not confidently one of the known classes
            return new Result("none", maxProb);
        } else {
            return new Result(labels.get(maxIndex), maxProb);
        }
    }
}
