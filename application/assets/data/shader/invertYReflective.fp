#ifdef GL_ES
#define LOWP lowp
precision highp float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_texture2;
uniform float time;
uniform float horizon;
uniform vec2 resolution;

float ripple(float coord, float t) {
    return sin(coord + t);
}

void main()
{
	vec2 inverted = vec2(v_texCoords.x, 1.0 - v_texCoords.y);
	vec4 sample1 = texture2D(u_texture, inverted);
	
	vec4 reflection = vec4(0, 0, 0, 0);
	float diff = 0.0;
	if (v_texCoords.y > horizon) {
		diff = (v_texCoords.y - horizon) * resolution.y;
	}
	
	float rippleX = 0.0;
	float rippleY = ripple(v_texCoords.y * resolution.y * 3.0 * max(0.0, (1.0 - (20.0 - diff)/20.0)), time * 3.0) * (2.0 * diff / 20.0);
	
	if (sample1.a > 0.0) {
		float xSample = (rippleX / resolution.x) + v_texCoords.x;
		float ySample = (rippleY / resolution.y) + v_texCoords.y - 2.0 * (horizon - 0.5);
		
		vec4 selfReflection = texture2D(
			u_texture, 
			vec2(
				xSample, 
				ySample
			)
		);
		
		if (v_texCoords.y - 2.0 * (horizon - 0.5) < horizon && selfReflection.a > 0.0) {
			reflection = selfReflection;
		} else {
			reflection = texture2D(
				u_texture2, 
				vec2(
					xSample, 
					ySample
				)
			);
		}
	}
	
	float c = max(0.0, resolution.y/2.0 - diff) / (resolution.y / 2.0);
	gl_FragColor = sample1 + reflection * vec4(c, c, c, 1.0);
}