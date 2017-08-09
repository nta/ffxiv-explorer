package com.fragmenterworks.ffxivextract.helpers;

public class ExportVertex {
    private float x;
    private float y;
    private float z;

    private float u;
    private float v;

    private float nx;
    private float ny;
    private float nz;

    private float[] weights;
    private int[] indices;

    public ExportVertex(float x, float y, float z, float u, float v, float nx, float ny, float nz, float[] weights, int[] indices) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.u = u;
        this.v = v;
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
        this.weights = weights;
        this.indices = indices;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getU() {
        return u;
    }

    public float getV() {
        return v;
    }

    public float getNx() {
        return nx;
    }

    public float getNy() {
        return ny;
    }

    public float getNz() {
        return nz;
    }

    public float[] getWeights() {
        return weights;
    }

    public int[] getIndices() {
        return indices;
    }
}
