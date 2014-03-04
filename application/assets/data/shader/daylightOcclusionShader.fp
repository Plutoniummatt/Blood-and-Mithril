#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec2 res;

uniform float alpha;

void main()
{
	vec4 color = texture2D(u_texture, v_texCoords);
	float alpha = color.a * 8.0;
	
	float i = 0.0;
	while (alpha == 8.0 && i < 13.0) {
		i = i + 1.0;
		float alpha1 = texture2D(u_texture, v_texCoords + vec2(2.0*i/res.x, 0.0)).a;
		float alpha2 = texture2D(u_texture, v_texCoords - vec2(2.0*i/res.x, 0.0)).a;
		float alpha3 = texture2D(u_texture, v_texCoords + vec2(0.0, 2.0*i/res.y)).a;
		float alpha4 = texture2D(u_texture, v_texCoords - vec2(0.0, 2.0*i/res.y)).a;
		float alpha5 = texture2D(u_texture, v_texCoords + vec2(2.0*i/res.x, 2.0*i/res.y)).a;
		float alpha6 = texture2D(u_texture, v_texCoords + vec2(-2.0*i/res.x, 2.0*i/res.y)).a;
		float alpha7 = texture2D(u_texture, v_texCoords + vec2(-2.0*i/res.x, -2.0*i/res.y)).a;
		float alpha8 = texture2D(u_texture, v_texCoords + vec2(2.0*i/res.x, -2.0*i/res.y)).a;
		
		alpha = (alpha1 + alpha2 + alpha3 + alpha4 + alpha5 + alpha6 + alpha7 + alpha8);
	}
	
	gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0 - i/13.0);
}