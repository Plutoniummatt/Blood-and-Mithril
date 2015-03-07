#ifdef GL_ES
#define LOWP lowp
precision highp float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform vec4 filter;
uniform vec4 toReplace;
uniform vec4 color;
uniform vec4 ignore;

void main()
{
	vec4 sampled = texture2D(u_texture, v_texCoords);
	
	if (sampled.r == toReplace.r && sampled.g == toReplace.g && sampled.b == toReplace.b && sampled.a == toReplace.a) {
		sampled = color;
	} else if (sampled.r != ignore.r && sampled.g != ignore.g && sampled.b != ignore.b) {
		sampled = sampled * filter;
	}
	
	gl_FragColor = sampled;
}