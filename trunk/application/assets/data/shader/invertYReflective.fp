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
uniform vec4 filter;

float ripple(float coord) {
    return sin(coord);
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
	
	float timeCoord = time;
	float spaceCoord = diff / (5 * atan(diff / 500) / (3.14159 / 2) + 1);
	
	float rippleX = 0.0;
	float rippleY = ripple(spaceCoord - timeCoord) * (2.0 * min(diff, 300.0) / 20.0);
	
	if (sample1.a > 0.0 && sample1.r == 1.0 && sample1.g == 0.0 && sample1.b == 0.0) {
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
			reflection = selfReflection * filter;
		} else {
			reflection = texture2D(
				u_texture2, 
				vec2(
					xSample, 
					ySample
				)
			);
		}
		
		sample1.r = 0.0;
		sample1.g = 0.0;
		sample1.b = 0.3;
	}
	
	float c = 0.85 * max(0.0, resolution.y/2.0 - diff) / (resolution.y / 2.0);
	gl_FragColor = sample1 * filter + reflection * vec4(c, c, c, 1.0);
}