#ifdef GL_ES
#define LOWP lowp
precision highp float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform vec4 color;
uniform vec2 sourceLocation;
uniform vec2 resolution;
uniform float time;

float rand(vec2 co) {
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * (time + 43758.5453));
}

void main()
{
	vec2 inverted = vec2(v_texCoords.x, 1.0 - v_texCoords.y);
	float alpha = 0.0;
	vec2 direction = normalize(sourceLocation - inverted * resolution);
	float d = distance(inverted * resolution, sourceLocation);
	
	float step = 1.0 + 15.0 * (d / 2000.0);
	for (int i = 0; i != 150; i++) {
		if (i * step > d) {
			break;
		}
		alpha = alpha + (1.0 - texture2D(u_texture, (inverted * resolution + direction * i * step) / resolution).a) / 10.0;
	}

	float r = rand(v_texCoords)/24.0;
	
	gl_FragColor = vec4(
		max(color.r, color.r / d * 200.0),
		max(color.g, color.g / d * 200.0),
		max(color.b, color.b / d * 200.0), 
		alpha * 9.0 / d
	) + vec4(0, 0, 0, min(r, r * alpha / d * 200.0));
}