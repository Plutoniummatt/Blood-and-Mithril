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

float rand(vec2 co) {
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * (time + 100)) - 1.0;
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
	
	if (sample1.a > 0.0) {
		reflection = texture2D(
			u_texture2, 
			vec2(
				rand(v_texCoords) * (0.04 + diff) * 20 / resolution.x + v_texCoords.x, 
				rand(inverted) * (0.04 + diff) * 20 / resolution.y + v_texCoords.y - (2.0 - diff) * (horizon - 0.5)
			)
		);
	}
	
	gl_FragColor = sample1 + reflection * max(0.0, 2.0 * (0.5-diff));
}