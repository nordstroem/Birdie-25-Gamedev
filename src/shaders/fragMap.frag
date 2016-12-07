#version 330

in vec2 fragUV;
in vec4 fragPos;
 
layout(location = 0) out vec4 outColor;

uniform sampler2D texSampler;
uniform vec4 color;

void main()
{
	float len = sqrt(fragPos.x*fragPos.x +  fragPos.y*fragPos.y);
    if(len < 1) {
    	outColor = color*texture(texSampler,fragUV);
    }else{
   		 outColor = vec4(0,0,0,0);
    }
	//outColor = vec4(0,1,0,1);
}
