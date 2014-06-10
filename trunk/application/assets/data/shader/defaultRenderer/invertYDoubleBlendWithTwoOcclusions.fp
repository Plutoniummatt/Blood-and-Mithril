#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D occlusion;
uniform sampler2D occlusion2;
uniform sampler2D occlusion3;
uniform sampler2D occlusion4;
uniform vec4 dayLightColor;

void main()
{
  vec2 inverted = vec2(v_texCoords.x, 1.0 - v_texCoords.y);
  vec4 sample2 = texture2D(occlusion2, inverted);
  
  float factor1 = texture2D(occlusion, inverted).g;
  float factor2 = sample2.r;
  float factor = factor1 * factor2;
  
  vec4 shadowSample = texture2D(occlusion3, inverted);
  vec4 backgroundOcclusionNearest = texture2D(occlusion4, inverted);
  
  float shadow = shadowSample.g;
  float extraShadow = sample2.r - sample2.g;
  float backgroundBlend = max(backgroundOcclusionNearest.g - backgroundOcclusionNearest.r, 0.0);
  vec4 foregroundDropShadow = vec4(0.0, 0.0, 0.0, 1.0) * (1.0 - shadow) * backgroundBlend * 0.5;
  vec4 extraForegroundDropShadow = vec4(0.0, 0.0, 0.0, 1.0) * max(0.0, extraShadow * backgroundBlend);
  
  vec4 sum = texture2D(u_texture, inverted) * vec4(factor, factor, factor, 1.0) * dayLightColor;
  vec4 sumWithShadow = sum + extraForegroundDropShadow;
  gl_FragColor = sumWithShadow;
}