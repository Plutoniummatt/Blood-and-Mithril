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

float rand(vec2 co) {
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main()
{
	float r = rand(v_texCoords)/256.0;
	float distanceFromHorizon = abs(v_texCoords.y - horizon) * resolution.y;
	
	gl_FragColor = ((1.0 - v_texCoords.y) * top + (v_texCoords.y) * bottom + vec4(r, r, r, 0.0)) / clamp((distanceFromHorizon + 500) / 1000, 0.0, 1.0);
}