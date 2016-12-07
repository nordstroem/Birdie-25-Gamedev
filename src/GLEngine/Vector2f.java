package GLEngine;

public class Vector2f {
	 
    public float x = 0.0f;
    public float y = 0.0f;
     
    public Vector2f(float x, float y){
        this.x = x;
        this.y = y;
    }
     
    public Vector2f(){ 
    	
    	
    }
    
    public float dot(Vector2f v){
    	return this.x*v.x + this.y*v.y;
    }
    
    public Vector2f normalized(){
    	float f = (float)Math.sqrt(this.x*this.x + this.y*this.y);
    	return new Vector2f(this.x/f, this.y/f);
    }
    


	public Vector2f subtract(Vector2f v) {
		return new Vector2f(this.x - v.x, this.y - v.y);
	}

	public Vector2f multiply(float i) {
		return new Vector2f(i*this.x, i*this.y);
	}
     
}