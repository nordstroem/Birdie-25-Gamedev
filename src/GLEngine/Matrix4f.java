package GLEngine;

import java.nio.FloatBuffer;

public class Matrix4f {

	public static final int SIZE = 4 * 4;
	public float[] elements = new float[SIZE];

	public Matrix4f() {
	}

	public static Matrix4f identity() {

		Matrix4f matrix = new Matrix4f();

		matrix.elements[0 + 0 * 4] = 1.0f;
		matrix.elements[1 + 1 * 4] = 1.0f;
		matrix.elements[2 + 2 * 4] = 1.0f;
		matrix.elements[3 + 3 * 4] = 1.0f;

		return matrix;
	}

	public static Matrix4f translate(Vector3f vector) {
		Matrix4f matrix = identity();
		matrix.elements[3 + 0 * 4] = vector.x;
		matrix.elements[3 + 1 * 4] = vector.y;
		matrix.elements[3 + 2 * 4] = vector.z;
		return matrix;
	}
	
	//elements[i + j*4];
	public static Matrix4f orthoProjection(float right, float left, float top, float bot, float near, float far){
		Matrix4f matrix = identity();
		matrix.elements[0 + 0*4] = 2/(right-left);
		matrix.elements[3 + 0*4] = -(right+left)/(right-left);
		matrix.elements[1 + 1*4] = 2/(top - bot);
		matrix.elements[3 + 1*4] = -(top + bot)/(top-bot);
		matrix.elements[2 + 2*4] = -2/(far-near);
		matrix.elements[3 + 2*4] = -(far + near)/(far -near);
		
		return matrix;
	}
	
	public static Matrix4f rotateX(float angle){
		Matrix4f matrix = identity();
		angle = (float)Math.toRadians(angle);
		matrix.elements[1 + 1*4] = (float)Math.cos(angle);
		matrix.elements[2 + 1*4] = -(float)Math.sin(angle);
		matrix.elements[1 + 2*4] = (float)Math.sin(angle);
		matrix.elements[2 + 2*4] = (float)Math.cos(angle);
		return matrix;
	}
	
	public static Matrix4f scale(float scale){
		Matrix4f matrix = identity();
		matrix.elements[0 + 0 * 4] = scale;
		matrix.elements[1 + 1 * 4] = scale;
		matrix.elements[2 + 2 * 4] = scale;
		return matrix;
	}
	
	public static Matrix4f scaleX(float scale){
		Matrix4f matrix = identity();
		matrix.elements[0 + 0 * 4] = scale;
		return matrix;
	}
	
	public static Matrix4f scaleY(float scale){
		Matrix4f matrix = identity();
		matrix.elements[1 + 1 * 4] = scale;
		return matrix;
	}
	
	public static Matrix4f scaleZ(float scale){
		Matrix4f matrix = identity();
		matrix.elements[2 + 2 * 4] = scale;
		return matrix;
	}
	
	public static Matrix4f rotateY(float angle){
		Matrix4f matrix = identity();
		angle = (float)Math.toRadians(angle);
		matrix.elements[0 + 0*4] = (float)Math.cos(angle);
		matrix.elements[2 + 0*4] = (float)Math.sin(angle);
		matrix.elements[0 + 2*4] = -(float)Math.sin(angle);
		matrix.elements[2 + 2*4] = (float)Math.cos(angle);
		return matrix;
	}
	
	public static Matrix4f rotateZ(float angle){ 
		Matrix4f matrix = identity();
		angle = (float)Math.toRadians(angle);
		matrix.elements[0 + 0*4] = (float)Math.cos(angle);
		matrix.elements[1 + 0*4] = -(float)Math.sin(angle);
		matrix.elements[0 + 1*4] = (float)Math.sin(angle);
		matrix.elements[1 + 1*4] = (float)Math.cos(angle);
		return matrix;
	}
	
	public static Matrix4f perspective(float fov, float aspect, float znear, float zfar){
		Matrix4f matrix = new Matrix4f();
		fov = (float) Math.toRadians(fov);
		matrix.elements[0 + 0*4] = fov/aspect;
		matrix.elements[1 + 1*4] = fov;
		matrix.elements[2 + 2*4] = (zfar + znear)/(znear - zfar);
		matrix.elements[3 + 2*4] = (2*zfar*znear)/(znear - zfar);
		matrix.elements[2 + 3*4] = -1;
		return matrix;
	}
	
	public static Matrix4f rotate(float angle) {
		Matrix4f matrix = identity();
		float r = (float) Math.toRadians(angle);
		float cos = (float) Math.cos(r);
		float sin = (float) Math.sin(r);

		matrix.elements[0 + 0 * 4] = cos;
		matrix.elements[1 + 0 * 4] = sin;
		matrix.elements[0 + 1 * 4] = -sin;
		matrix.elements[1 + 1 * 4] = cos;
		return matrix;
	}
	
	public static Matrix4f bias(){
		Matrix4f matrix = identity();
		matrix.elements[0 + 0 * 4] = 0.5f;
		matrix.elements[1 + 1 * 4] = 0.5f;
		matrix.elements[2 + 2 * 4] = 0.5f;
		matrix.elements[3 + 0 * 4] = 0.5f;
		matrix.elements[3 + 1 * 4] = 0.5f;
		matrix.elements[3 + 2 * 4] = 0.5f;
		return matrix;	
	}

	public static Matrix4f lookAt(Vector3f eye, Vector3f center, Vector3f up){
		Matrix4f matrix = identity();
		Vector3f f = center.subtract(eye);
		f = f.normalized();
		up = up.normalized();
		Vector3f s = f.cross(up);
		Vector3f u = s.cross(f);
		
		matrix.elements[0 + 0*4] = s.x;
		matrix.elements[1 + 0*4] = s.y;
		matrix.elements[2 + 0*4] = s.z;
		
		matrix.elements[0 + 1*4] = u.x;
		matrix.elements[1 + 1*4] = u.y;
		matrix.elements[2 + 1*4] = u.z;
		
		matrix.elements[0 + 2*4] = -f.x;
		matrix.elements[1 + 2*4] = -f.y;
		matrix.elements[2 + 2*4] = -f.z;
		
		Matrix4f trans = Matrix4f.translate(eye.multiply(-1));
		
		return matrix.multiply(trans);
	}
	
	public Matrix4f multiply(Matrix4f matrix) {
		Matrix4f result = new Matrix4f();
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				float sum = 0.0f;
				for (int k = 0; k < 4; k++) {
					sum += this.elements[k + y * 4]* matrix.elements[x + k * 4];
				}
				result.elements[x + y * 4] = sum;
			}
		}
		return result;
	}

	
	public FloatBuffer toFloatBuffer() {
		return Utilities.createFloatBuffer(elements);
	}

}