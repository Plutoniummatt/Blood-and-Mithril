#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform vec4 toReplace;
uniform vec4 color;

void main()
{
	vec4 sampled = texture2D(u_texture, v_texCoords);
	
	if (sampled.r == toReplace.r && sampled.g == toReplace.g && sampled.b == toReplace.b) {
		sampled = color;
	}
	
	gl_FragColor = sampled;
}