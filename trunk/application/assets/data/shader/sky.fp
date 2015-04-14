#ifdef GL_ES
#define LOWP lowp
precision highp float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform vec4 top;
uniform vec4 bottom;
uniform float horizon;
uniform vec2 resolution;
uniform vec2 sun;

float rand(vec2 co) {
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main()
{
	float r = rand(v_texCoords)/256.0;
	float distanceFromHorizon = abs(v_texCoords.y - horizon) * resolution.y;
	float distanceFromSun = abs(v_texCoords.x * resolution.x - sun.x);
	
	float filter = 1.0 - min(
		1.0, 
		distanceFromHorizon / 200.0 / pow(cos(distanceFromSun / 2500), 2)
	);
	
	gl_FragColor = ((1.0 - v_texCoords.y) * top + (v_texCoords.y) * bottom + vec4(r, r, r, 0.0)) + vec4(filter * 0.5, 0.2 * filter, 0.0, filter) * bottom.b;
}