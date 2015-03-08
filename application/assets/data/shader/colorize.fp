#ifdef GL_ES
#define LOWP lowp
precision highp float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform vec4 color;
uniform float amount;

void main()
{
	vec4 sampled = texture2D(u_texture, v_texCoords);
	
	vec4 fin = 
		sampled * color * min(1.0, (1.0 - sampled.r) * amount) 
		+ 
		sampled * (1.0 - min(1.0, (1.0 - sampled.r) * amount));
		
	gl_FragColor = vec4(fin.rgb, sampled.a);
}