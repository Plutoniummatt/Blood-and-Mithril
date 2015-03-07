#ifdef GL_ES
#define LOWP lowp
precision highp float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec4 color;

void main()
{
	vec4 sampled = texture2D(u_texture, v_texCoords);
	float maxLength = max(sampled.r, max(sampled.g, sampled.b));
	
	if (maxLength == 0.0) {
		maxLength = 1.0;
	}
	
	gl_FragColor = sampled * vec4(
		1.0 / maxLength, 
		1.0 / maxLength, 
		1.0 / maxLength, 
		maxLength / sqrt(3.0)
	) * color;
}