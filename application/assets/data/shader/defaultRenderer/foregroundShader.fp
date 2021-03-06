#ifdef GL_ES
#define LOWP lowp
precision highp float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D occlusion;
uniform sampler2D occlusion2;
uniform sampler2D occlusion3;
uniform sampler2D mgLighting;
uniform vec4 dayLightColor;

void main()
{
	vec2 inverted = vec2(v_texCoords.x, 1.0 - v_texCoords.y);

	float factor1 = texture2D(occlusion, inverted).g;
	float factor2 = texture2D(occlusion2, inverted).r;
	float factor = factor1 * factor2;

	vec4 fg = texture2D(u_texture, inverted);
	vec4 lighting = texture2D(occlusion3, v_texCoords);
	vec4 mLighting = texture2D(mgLighting, v_texCoords);

	if (length(mLighting.rgb) > 0.4) {
		mLighting = vec4(normalize(mLighting.rgb) * 0.4, mLighting.a);
	}
	
	vec4 combinedFactor = (vec4(factor, factor, factor, 1.0) * dayLightColor + lighting + mLighting) * vec4(factor1, factor1, factor1, 1.0);
	
	combinedFactor.a = min(combinedFactor.a, 1.0);
	
	vec4 sum = fg * combinedFactor;

	gl_FragColor = sum;
}