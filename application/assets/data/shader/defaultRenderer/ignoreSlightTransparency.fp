#ifdef GL_ES
#define LOWP lowp
precision highp float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;

void main()
{
	vec4 a = texture2D(u_texture, v_texCoords);
	
	float multiplier = 1.0;
	
	if (a.a < 1.0) {
		multiplier = 0.0;
	}
	
	gl_FragColor = a * vec4(multiplier, multiplier, multiplier, multiplier);
}