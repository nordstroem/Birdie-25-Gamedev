#version 330

in vec2 fragUV;

layout(location = 0) out vec4 outColor;

uniform sampler2D texSampler;
uniform vec4 color;

void main()
{
    
    outColor = color*texture(texSampler,fragUV);
	//outColor = vec4(0,1,0,1);
}
