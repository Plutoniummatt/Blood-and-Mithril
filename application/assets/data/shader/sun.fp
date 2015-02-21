#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec2 sunPosition;
uniform vec2 resolution;
uniform vec4 filter;
uniform float epoch;
uniform float nightSuppression;

void main()
{
	vec2 inverted = vec2(v_texCoords.x, -v_texCoords.y + 1);
	vec4 tex = texture2D(u_texture, v_texCoords);
	
	float distanceFromSun = distance(inverted * resolution, sunPosition);
	
	vec2 invertedResolution = inverted * resolution;
	float distanceFromLensFlare = distance(invertedResolution, sunPosition);
	float func = 2 - distanceFromSun / 1300;
	float func2 = 100 / distanceFromSun;
	
	vec4 color = filter * max(func, 0); 
	vec4 color2 = filter * vec4(1.0, 0.7, 0.7, 1.0) * max(func2, 0);
	
	gl_FragColor = 
		tex * color * filter.a + 
		tex + 
		color2;
}