#version 330

#define DEPTH_OFFSET 0.00005

in vec2 fragUV;
in vec3 fragNormal;
in vec3 fragPos;
in vec4 shadowCoord;

out vec4 outColor;

uniform sampler2D texSampler;
uniform sampler2D shadowMap;

uniform vec4 color;
uniform vec3 camPos;
uniform vec3 specularColor;


void main()
{
	vec4 lightColor = vec4(1.0,1.0,1.0,1.0);
    vec3 lightDir = normalize(vec3(0,-0.5,-0.5));
    vec3 lightDirSpec = normalize(vec3(0, 0, -1));
	vec3 camToFrag = camPos - fragPos; 
	vec3 camToFragUnit = normalize(camToFrag);
	vec3 reflectVector = reflect(lightDirSpec, fragNormal);

	
	float amb = 0.6;
    float diffuseFactor = clamp(dot(fragNormal,-lightDir), 0.0, 1);
    vec4 ambient = vec4(amb,amb,amb,1.0);
    vec4 diffuse = lightColor*diffuseFactor;
    
    float cosTheta = clamp(dot(lightDir, fragNormal),0,1);
	float bias = 0.005*tan(acos(cosTheta)); // cosTheta is dot( n,l ), clamped between 0 and 1
	bias = clamp(bias, 0, 0.001);
	float visibility = 1.0;

	if ( texture( shadowMap, shadowCoord.xy ).z  <  shadowCoord.z - bias && length(camToFrag) < 800){
   		visibility = 0.5;
	}

	float specularFactor = dot(reflectVector, camToFragUnit);
	specularFactor = max(specularFactor,0.0);
    	float dampedFactor = 0;
	if(diffuseFactor > 0.0){
		dampedFactor = pow(specularFactor,34);
	}
	vec4 specular = dampedFactor * vec4(specularColor.xyz, 1.0);
	
   // float test = 1/(1 + 0.01*length(camToFrag) + 0.0001*pow(length(camToFrag),2));
    outColor = color*texture(texSampler,fragUV)*(ambient + visibility*diffuse) + specular*0.8;

    outColor = outColor + 0.0001*vec4(camPos, 1) + 0.0001*texture(texSampler, fragUV) + 0.00001*vec4(specularColor,1); //Dummy
}
