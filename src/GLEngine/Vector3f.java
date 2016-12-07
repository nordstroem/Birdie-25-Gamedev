package GLEngine;

public class Vector3f {
	 
    public float x = 0.0f;
    public float y = 0.0f;
    public float z = 0.0f;
     
    public Vector3f(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
     
    public Vector3f(){ 
    	
    }
    
    public static Vector3f toVector(javax.vecmath.Vector3f v){
    	return new Vector3f(v.x, v.y, v.z);
    }
    
    public float dot(Vector3f v){
    	return this.x*v.x + this.y*v.y + this.z*v.z;
    }
    
    public Vector3f normalized(){
    	float f = (float)Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
    	return new Vector3f(this.x/f, this.y/f, this.z/f);
    }
    
    public Vector3f cross(Vector3f v){
    	Vector3f vec = new Vector3f();
    	vec.x = this.y*v.z - this.z*v.y;
    	vec.y = this.z*v.x - this.x*v.z;
    	vec.z = this.x*v.y - this.y*v.x;
    	return vec;
    	
    }

	public Vector3f subtract(Vector3f v) {
		return new Vector3f(this.x - v.x, this.y - v.y, this.z - v.z);
	}

	public Vector3f multiply(float i) {
		return new Vector3f(i*this.x, i*this.y, i*this.z);
	}
	
	public float distanceTo(Vector3f v) {
		float dx = x - v.x;
		float dy = y - v.y;
		float dz = z - v.z;
		return (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
	}

	public Vector3f add(Vector3f v) {
		return new Vector3f(this.x + v.x, this.y + v.y, this.z + v.z);
	}

	public float length() {
		return (float)Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
	}
     
}