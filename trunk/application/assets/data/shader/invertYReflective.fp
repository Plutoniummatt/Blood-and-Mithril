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
    return sin(coord * 7000 + t);
}

float rand(vec2 co) {
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * (43758.5453));
}

float tRand(vec2 co) {
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * (time + 43758.5453));
}

void main()
{
	vec2 inverted = vec2(v_texCoords.x, 1.0 - v_texCoords.y);
	vec4 sample1 = texture2D(u_texture, inverted);
	
	vec4 reflection = vec4(0, 0, 0, 0);
	float diff = 0.0;
	if (v_texCoords.y > horizon) {
		diff = v_texCoords.y - horizon;
	}
	
	float rippleX = ripple(v_texCoords.x + rand(vec2(0.0, v_texCoords.y)), time * 30 * (0.1 + diff)) * (0.3 + diff * 20.0) + tRand(inverted) * 3.0;
	float rippleY = 3.0 * tRand(v_texCoords);
	
	if (sample1.a > 0.0) {
		reflection = texture2D(
			u_texture2, 
			vec2(
				(rippleX / resolution.x) + v_texCoords.x, 
				(rippleY / resolution.y) + v_texCoords.y - 2.0 * (horizon - 0.5)
			)
		);
	}
	
	gl_FragColor = sample1 + reflection;
}