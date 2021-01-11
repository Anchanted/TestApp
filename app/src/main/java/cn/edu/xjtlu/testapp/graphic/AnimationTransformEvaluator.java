package cn.edu.xjtlu.testapp.graphic;

import android.animation.TypeEvaluator;

public class AnimationTransformEvaluator implements TypeEvaluator<AnimationTransform> {
    @Override
    public AnimationTransform evaluate(float fraction, AnimationTransform startValue, AnimationTransform endValue) {
        float currentTranslateX = startValue.translateX + fraction * (endValue.translateX - startValue.translateX);
        float currentTranslateY = startValue.translateY + fraction * (endValue.translateY - startValue.translateY);
        float currentScaleX = startValue.scaleX + fraction * (endValue.scaleX - startValue.scaleX);
        float currentScaleY = startValue.scaleY + fraction * (endValue.scaleY - startValue.scaleY);
        return new AnimationTransform(currentTranslateX, currentTranslateY, currentScaleX, currentScaleY);
    }
}
