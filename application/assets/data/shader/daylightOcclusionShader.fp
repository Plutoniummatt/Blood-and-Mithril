#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec2 res;

uniform vec4 dl;

void main()
{
	vec4 color = texture2D(u_texture, v_texCoords);
	float alpha = min(color.a, min(texture2D(u_texture, v_texCoords + vec2(1.0/res.x, 2.0/res.y)).a, texture2D(u_texture, v_texCoords + vec2(2.0/res.x, 0.0)).a));
	
	float i = 0.0;
	while (alpha == 1.0 && i < 20.0) {
		i = i + 1.0;
		float alpha1 = texture2D(u_texture, v_texCoords + vec2(3.0*i/res.x, 0.0)).a;
		float alpha2 = texture2D(u_texture, v_texCoords - vec2(3.0*i/res.x, 0.0)).a;
		float alpha3 = texture2D(u_texture, v_texCoords + vec2(0.0, 3.0*i/res.y)).a;
		float alpha4 = texture2D(u_texture, v_texCoords - vec2(0.0, 3.0*i/res.y)).a;
		float alpha5 = texture2D(u_texture, v_texCoords + vec2(3.0*i/res.x, 3.0*i/res.y)).a;
		float alpha6 = texture2D(u_texture, v_texCoords + vec2(-3.0*i/res.x, 3.0*i/res.y)).a;
		float alpha7 = texture2D(u_texture, v_texCoords + vec2(-3.0*i/res.x, -3.0*i/res.y)).a;
		float alpha8 = texture2D(u_texture, v_texCoords + vec2(3.0*i/res.x, -3.0*i/res.y)).a;
		
		alpha = min(
			alpha1, 
			min(alpha2, 
				min(alpha3, 
					min(alpha4, 
						min(alpha5, 
							min(alpha6, 
								min(alpha7, 
									alpha8
								)
							)
						)
					)
				)
			)
		);
	}
	
	float value;
	if (alpha == 1.0) {
		value = 1.0;
	} else {
		value = 1.0 - alpha;
	}
	
	gl_FragColor = vec4(1.0, 1.0, 1.0, value * (1.0 - i / 20.0)) * dl;
}