#ifdef GL_ES
#define LOWP lowp
precision highp float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D occlusion3;
uniform sampler2D occlusion4;
uniform sampler2D occlusion5;
uniform sampler2D mgLighting;
uniform vec4 dayLightColor;

void main()
{
	vec2 inverted = vec2(v_texCoords.x, 1.0 - v_texCoords.y);
	float factor = texture2D(occlusion3, inverted).r;
	vec4 particle = texture2D(occlusion5, v_texCoords) + texture2D(mgLighting, v_texCoords);
  
	vec4 sample3 = texture2D(occlusion3, inverted);
	vec4 backgroundOcclusionNearest = texture2D(occlusion4, inverted);
	float extraShadow = sample3.r - sample3.g;
	float backgroundBlend = max(backgroundOcclusionNearest.g - backgroundOcclusionNearest.r, 0.0);
	vec4 foregroundDropShadow = vec4(1.0, 1.0, 1.0, 0.0) * max(0.0, extraShadow * backgroundBlend) * 0.3;
	
	vec4 bg = texture2D(u_texture, inverted);
	vec4 sampleBlendedWithDaylight = texture2D(u_texture, inverted) * (vec4(factor, factor, factor, 1.0) * dayLightColor + particle);
	
	vec4 sum = sampleBlendedWithDaylight - sampleBlendedWithDaylight * 5.0 * foregroundDropShadow;
	
	gl_FragColor = sum;
}