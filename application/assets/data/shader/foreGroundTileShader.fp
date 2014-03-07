#define PI 3.14

#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_texture2;

uniform vec2 resolution;
uniform vec4 color;
uniform float penetration;

//sample from the 1D distance map
float sample(vec2 coord, float r) {
	return step(r, texture2D(u_texture2, coord).r);
}

//sample from the 1D distance map
float sample2(vec2 coord) {
	return texture2D(u_texture2, coord).r;
}

float illumination() {
	//rectangular to polar
	vec2 norm = v_texCoords.xy * 2.0 - 1.0;
	float theta = atan(norm.y, norm.x);
	float r = length(norm);	
	float coord = (theta + PI) / (2.0*PI);
	
	//the tex coord to sample our 1D lookup texture	
	//always 0.0 on y axis
	vec2 tc = vec2(coord, 0.0);
	
	//the center tex coord, which gives us hard shadows
	float center = sample(1-tc, r + 0.001);        
	
	//we multiply the blur amount by our distance from center
	//this leads to more blurriness as the shadow "fades away"
	float blur = (1./resolution.x)  * smoothstep(0., 1., r); 
	
	//now we use a simple gaussian blur
	float sum = 0.0;
	
	if (center == 1) {
		sum += 0;
	} else {
		sum += 1 - (r - sample2(1-tc))/penetration;
	}
	
	//sum of 1.0 -> in light, 0.0 -> in shadow
 	
 	//multiply the summed amount by our distance, which gives us a radial falloff
 	//then multiply by vertex (light) color
 	float falloff = 1;
 	if (r > 0.5) {
 		falloff = max((1 - r), 0) / 0.5;
 	}
 	return sum * falloff;
}

void main()
{
	float illumination = illumination();
	vec3 sample = texture2D(u_texture, v_texCoords);
	gl_FragColor = vec4(
		max(sample.r, sample.r * illumination),
		max(sample.g, sample.g * illumination),
		max(sample.b, sample.b * illumination), 
		min(illumination, texture2D(u_texture, v_texCoords).a)
	) * color;
}