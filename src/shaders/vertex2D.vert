
#version 330

layout ( location = 0) in vec2 position;
layout ( location = 1) in vec2 uv;

out vec2 fragUV;
uniform mat4 PV;
uniform mat4 M;

void main()
{
	vec4 pos = PV*M*vec4(position.x, position.y, 0.0, 1.0);
    gl_Position = vec4(pos.x, pos.y, 1.0, 1.0);
    fragUV = vec2(uv.x, 1 - uv.y);
	
}