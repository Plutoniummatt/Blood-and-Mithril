#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_texture2;

uniform vec2 lightSource;
uniform vec2 resolution;
uniform float size;

float lighting() {
	vec2 position = v_texCoords * resolution;
	float dist = length(position - lightSource);
	
	return max(1 - dist*2/size, 0);
}

void main()
{
	float lighting = lighting();
	vec4 sample = texture2D(u_texture, v_texCoords);
	vec4 sample2 = texture2D(u_texture2, v_texCoords);
	vec4 radialLighting = sample * lighting * 0.85;
	
	float a = 1.0;
	if (sample2.a < 0.1) {
		a = sample2.a;
	}
	
	gl_FragColor = vec4(
		max(radialLighting.r, sample.r * sample2.r) * max(sample2.a, lighting * 0.35), 
		max(radialLighting.g, sample.g * sample2.g) * max(sample2.a, lighting * 0.35), 
		max(radialLighting.b, sample.b * sample2.b) * max(sample2.a, lighting * 0.35),
		max(lighting * sample.a, a * sample.a)
	);
}