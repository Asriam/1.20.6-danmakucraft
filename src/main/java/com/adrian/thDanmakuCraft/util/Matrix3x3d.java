package com.adrian.thDanmakuCraft.util;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3d;

public class Matrix3x3d {
    private final double[][] values;

    public Matrix3x3d(double x1, double y1, double z1,
                      double x2, double y2, double z2, 
                      double x3, double y3, double z3) {

        this.values = new double[][] {
                {x1, y1, z1},
                {x2, y2, z2},
                {x3, y3, z3}
        };
    }

    public Matrix3x3d(double [][] values){
        this.values = values;
    }

    public Vec3 multiply(Vec3 vec) {
        double x = vec.x * values[0][0] + vec.y * values[0][1] + vec.z * values[0][2];
        double y = vec.x * values[1][0] + vec.y * values[1][1] + vec.z * values[1][2];
        double z = vec.x * values[2][0] + vec.y * values[2][1] + vec.z * values[2][2];
        return new Vec3(x, y, z);
    }

    public Matrix3x3d multiply(Matrix3x3d matrix) {
        double[][] result = new double[3][3];

        result[0][0] = this.values[0][0] * matrix.values[0][0] 
                     + this.values[0][1] * matrix.values[1][0] 
                     + this.values[0][2] * matrix.values[2][0];
        result[0][1] = this.values[0][0] * matrix.values[0][1] 
                     + this.values[0][1] * matrix.values[1][1] 
                     + this.values[0][2] * matrix.values[2][1];
        result[0][2] = this.values[0][0] * matrix.values[0][2] 
                     + this.values[0][1] * matrix.values[1][2] 
                     + this.values[0][2] * matrix.values[2][2];

        result[1][0] = this.values[1][0] * matrix.values[0][0] 
                     + this.values[1][1] * matrix.values[1][0] 
                     + this.values[1][2] * matrix.values[2][0];
        result[1][1] = this.values[1][0] * matrix.values[0][1] 
                     + this.values[1][1] * matrix.values[1][1] 
                     + this.values[1][2] * matrix.values[2][1];
        result[1][2] = this.values[1][0] * matrix.values[0][2] 
                     + this.values[1][1] * matrix.values[1][2] 
                     + this.values[1][2] * matrix.values[2][2];

        result[2][0] = this.values[2][0] * matrix.values[0][0] 
                     + this.values[2][1] * matrix.values[1][0] 
                     + this.values[2][2] * matrix.values[2][0];
        result[2][1] = this.values[2][0] * matrix.values[0][1] 
                     + this.values[2][1] * matrix.values[1][1] 
                     + this.values[2][2] * matrix.values[2][1];
        result[2][2] = this.values[2][0] * matrix.values[0][2] 
                     + this.values[2][1] * matrix.values[1][2] 
                     + this.values[2][2] * matrix.values[2][2];

        return new Matrix3x3d(result);
    }

    public Matrix3x3d transpose() {
        double[][] transposed = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                transposed[i][j] = values[j][i];
            }
        }
        return new Matrix3x3d(transposed);
    }
}
